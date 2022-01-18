//
//  PanoPoolService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolService.h"
#import "PanoCallClient.h"
#import "NSArray+Extension.h"


static NSUInteger PoolFirstPageMaxCount = 2;

static NSUInteger PoolOtherPageMaxCount = 4;

@interface PanoPoolService () <PanoUserDelegate>{
    __weak PanoUserService *_userService;
    PageIndex _index;
    NSMutableArray *_dataSource;
    NSMutableArray<PanoViewInstance *> *_desktopInstances;
}

@property (assign, nonatomic) BOOL isActivingVoice; ///< 是否开启语音激励
@property (assign, nonatomic) BOOL isSwitchFlag;
@property (strong, nonatomic) NSMutableDictionary<NSNumber *, NSNumber*> *speakingUsers;
@property (strong, nonatomic) NSDate *lastSpeakDate;
@property (assign, nonatomic) NSInteger activeAudioUserID;
@property (strong, nonatomic) PanoViewPage *currentPage;
@property (strong, nonatomic, readonly) PanoViewInstance *desktopInstance;
@property (strong, nonatomic) PanoViewInstance *pinInstance;
@property (strong, nonatomic) NSMutableArray<NSNumber *> *myActiveAudios;
@property (strong, nonatomic) NSDate *lastMySpeakDate;
@end

@implementation PanoPoolService

- (void)start {
    _index = 0;
    _dataSource = [NSMutableArray array];
    _desktopInstances = [NSMutableArray array];
    _myActiveAudios = [NSMutableArray array];
    _enableRender = true;
    _userService = PanoCallClient.shared.userMgr;
}

- (void)stopRender{
    _enableRender = false;
    [self.delegate onPoolMediaChanged: [[PanoViewPage alloc] init]];
}

- (void)startRender {
    _enableRender = true;
    [self notify];
}

- (PanoViewInstance *)videoInstanceWithUser:(nonnull PanoUserInfo *)user {
    PanoViewInstance *instance = [[PanoViewInstance alloc] init];
    instance.type = PanoViewInstance_Video;
    instance.userId = user.userId;
    return instance;
}

- (PanoViewPageLayoutType)layoutType:(NSUInteger)len {
    if (len == 1) {
        return PanoViewPageLayout_FullScreen;
    } else if (len == 2 && _index == 0) {
        // 演讲者模式显示 浮动布局
        return PanoViewPageLayout_Float;
    } else if (len == 3 || len == 4 || (len == 2 && _index > 0)) {
        // 画廊模式显示 平均布局
        return PanoViewPageLayout_4_Avg;
    } else {
        NSLog(@"NOT SUPPORT");
        return PanoViewPageLayout_FullScreen;
    }
}

- (NSArray<PanoViewInstance *> *)data {
    NSMutableArray *mArray = [NSMutableArray array];
    [mArray addObjectsFromArray:_dataSource];
    [mArray addObjectsFromArray:self.otherDesktopInstances];
    return mArray;
}

/// 优先级最高的桌面共享实例
- (PanoViewInstance *)desktopInstance {
    return _desktopInstances.firstObject;
}

/// 其它所有的桌面共享实例
- (NSArray *)otherDesktopInstances {
    UInt64 deskInstanceUserId = self.desktopInstance.userId;
    return [_desktopInstances filteredArrayUsingBlock:^BOOL(PanoViewInstance * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        return obj.userId != deskInstanceUserId;
    }];
}

- (void)notify {
    if (!_enableRender) {
        return;
    }
    if ([self.delegate respondsToSelector:@selector(onPoolMediaChanged:)]) {
        PanoViewPage *page = [[PanoViewPage alloc] init];
        if (_index == 0) {
            NSInteger len = self.data.count >= 2 ? PoolFirstPageMaxCount : self.data.count;
            NSArray * array = [self.data subarrayWithRange:NSMakeRange(0, len)];
            // 1. 查找到正在语音激励的用户
            PanoViewInstance *activeAudioInstance = nil;
            for (PanoViewInstance *instance in self.data) {
                if (instance.userId == _activeAudioUserID && instance.type == PanoViewInstance_Video) {
                    activeAudioInstance = instance;
                    break;
                }
            }
            // 如果谁开启了批注
            if (_pinInstance != nil) {
                page.instances = @[_pinInstance, self.data.firstObject];
            } else if (self.desktopInstance != nil) {
                page.instances = @[self.desktopInstance, self.activeSpeakerUser];
            }
            // 2. 激励用户 生成对应的数组
            else if (_isActivingVoice && activeAudioInstance) {
                page.instances = @[activeAudioInstance, self.data.firstObject];
            } else {
                array = [[array reverseObjectEnumerator] allObjects];
                // 3. 双击切换过布局
                if (_isSwitchFlag) {
                    array = [[array reverseObjectEnumerator] allObjects];
                }
                page.instances = array;
            }
            page.type = [self layoutType:page.instances.count];
            if (page.instances.count == 2) {
                page.instances.firstObject.mode = PanoViewInstance_Max;
                page.instances.lastObject.mode = PanoViewInstance_Float;
            } else {
                page.instances.firstObject.mode = PanoViewInstance_Max;
            }
        } else {
            NSMutableArray *mArray = [NSMutableArray array];
            [mArray addObject:self.data.firstObject];
            NSUInteger loc = 1 + (_index - 1) * (PoolOtherPageMaxCount - 1);
            NSUInteger len = self.data.count - loc > (PoolOtherPageMaxCount - 1) ? (PoolOtherPageMaxCount - 1) : self.data.count - loc;
            [mArray addObjectsFromArray:[self.data subarrayWithRange:NSMakeRange(loc, len)]];
            for (PanoViewInstance *instance in mArray) {
                instance.mode = PanoViewInstance_Avg;
            }
            page.instances = mArray;
            page.type = [self layoutType:mArray.count];
        }
        self.currentPage = page;
        [self.delegate onPoolMediaChanged:page];
    }
}

- (void)switchInstance:(PanoViewInstance *)instance {
    _isSwitchFlag = !_isSwitchFlag;
    [self notify];
}

- (void)togglePinViewInstance:(PanoViewInstance *)instance completion:(nonnull void (^)(void))completion {
    if (instance.type == PanoViewInstance_Desktop) {
        //TODO: 交换屏幕共享的位置
        NSUInteger index = [_desktopInstances indexOfObject:instance];
        if (index != NSNotFound) {
            [_desktopInstances exchangeObjectAtIndex:0 withObjectAtIndex:index];
            _index = 0;
            [self notify];
        }
        completion();
    } else if (instance.userId != _userService.me.userId && instance.type == PanoViewInstance_Video && _desktopInstances.count == 0) {
        // 如果这个实例类型是视频, 且没有桌面共享
        _pinInstance = instance;
        _index = 0;
        [self notify];
        completion();
    }
}

- (void)cancelPinViewInstance:(PanoViewInstance *)instance completion:(void (^)(void))completion {
    if (instance.type == PanoViewInstance_Video && instance.userId == _pinInstance.userId) {
        _pinInstance = nil;
        completion();
    }
}

- (PageIndex)currentIndex {
    return _index;
}

/// 返回UI上显示的页码数
- (PageIndex)numbersOfIndexs {
    if (self.data.count == 2 && [self isSharing]) {
        return 2;
    }
    if (self.data.count <= 2) {
        return 1;
    }
    PageIndex result = (PageIndex)ceil((self.data.count - 1) / 3.0) + 1;
    return result;
}

/// 是否正在开启共享
- (BOOL)isSharing {
    return self.desktopInstance != nil;
}

#pragma mark -- PanoUserDelegate

- (void)onUserAdded:(nonnull PanoUserInfo *)user {
    PanoViewInstance *instance = [self videoInstanceWithUser:user];
    [_dataSource addObject:instance];
    [self notify];
}

- (void)onUserRemoved:(nonnull PanoUserInfo *)user {
    PanoViewInstance *instance = [self videoInstanceWithUser:user];
    PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:user.userId type:PanoViewInstance_Desktop];
    [_desktopInstances removeObject:desktopInstance];
    [_dataSource removeObject:instance];
    [self updatePageIndex];
    if (user.userId == _activeAudioUserID) {
        _activeAudioUserID = 0;
    }
    if (_pinInstance && user.userId == _pinInstance.userId) {
        _pinInstance = nil;
    }
    [self notify];
}

- (void)onUserStatusChanged:(nonnull PanoUserInfo *)user {
    [self notify];
    if (user.userId == self.activeSpeakerUser.userId) {
        PanoViewInstance *instance = [[PanoViewInstance alloc] init];
        instance.type = PanoViewInstance_Video;
        instance.userId = user.userId;
        if ([self.delegate respondsToSelector:@selector(onAudioActiveUserStatusChanged:)]) {
            [self.delegate onAudioActiveUserStatusChanged:instance];
        }
    }
    if (user.userId == PanoCallClient.shared.userId &&
        user.videoStaus == PanoUserVideo_Unmute) {
        PanoViewInstance *myInstance = [[PanoViewInstance alloc] initWithUserId:PanoCallClient.shared.userId type:PanoViewInstance_Video];
        if (![self.currentPage.instances containsObject:myInstance]) {
            NSLog(@"open My Video");
            [PanoCallClient.shared.video startVideo];
        }
    }
}

- (void)onUserDidEndShareingScreen:(PanoUserInfo *)user {
    PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:user.userId type:PanoViewInstance_Desktop];
    [_desktopInstances removeObject:desktopInstance];
    [self updatePageIndex];
    [self notify];
}

- (void)onUserDidBeginSharingScreen:(PanoUserInfo *)user {
    _pinInstance = nil;
    PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:user.userId type:PanoViewInstance_Desktop];
    if (![_desktopInstances containsObject:desktopInstance]) {
        [_desktopInstances insertObject:desktopInstance atIndex:0];
    }
    _index = 0;
    [self notify];
}

- (void)updatePageIndex {
    while (_index >= [self numbersOfIndexs]) {
        _index--;
    }
}

#pragma mark -- PanoRtcEngineDelegate

- (void)onUserAudioLevel:(PanoRtcAudioLevel * _Nonnull)level {
    if (!_speakingUsers) {
        _speakingUsers = [NSMutableDictionary dictionaryWithCapacity:4];
    }
    [_speakingUsers setObject:@(level.level) forKey:@(level.userId)];
    if (!_lastSpeakDate) {
        _lastSpeakDate = [NSDate date];
    }
    if ([[NSDate date] timeIntervalSinceDate:_lastSpeakDate] > 1.0) {
        _lastSpeakDate = [NSDate date];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self startAtiveAudio];
        });
    }
    if (level.userId == PanoCallClient.shared.userId) {
        if (!_lastMySpeakDate) {
            _lastMySpeakDate = [NSDate date];
        }
        [_myActiveAudios addObject:@(level.active)];
        if (_myActiveAudios.count >= 300) {
            NSInteger activeCount = 0;
            for (NSNumber *value in _myActiveAudios) {
                activeCount += value.integerValue;
            }
            [_myActiveAudios removeAllObjects];
            if (activeCount >= 240) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.delegate onSpeakingWhenTheAudioMuted];
                });
            }
        }
    }
}

- (void)startAtiveAudio {
    if ([self.delegate respondsToSelector:@selector(onMyAudioActiveChanged:)]) {
        NSInteger level = [self->_speakingUsers[@(PanoCallClient.shared.userId)] integerValue];
        [self.delegate onMyAudioActiveChanged: level > 500];
    }
    
    // TODO 更新未说话语音
    if (self->_activeAudioUserID > 0) {
        _isActivingVoice = true;
    }
    BOOL activing = false;
    if (self.activeSpeakerList.firstObject) {
        NSInteger level = [[self->_speakingUsers objectForKey:self.activeSpeakerList.firstObject] integerValue];
        activing  = level > 500;
    }
    
    if ([self.delegate respondsToSelector:@selector(onAudioActiveUserChanged:activing:)]) {
        PanoViewInstance *instance = nil;
        if (self.activeSpeakerList.count > 0) {
            instance = [[PanoViewInstance alloc] init];
            instance.type = PanoViewInstance_Video;
            instance.userId = self.activeSpeakerList.firstObject.integerValue;
        }
        [self.delegate onAudioActiveUserChanged:instance activing:activing];
    }
    
    // 1. 如果当前在第一页 需要替换掉说话的用户
    if (_index == 0) {
        [self notify];
    }
}

- (PanoViewInstance *)activeSpeakerUser {
    PanoViewInstance *instance = [[PanoViewInstance alloc] init];
    instance.type = PanoViewInstance_Video;
    instance.userId = PanoCallClient.shared.userId;
    return  instance;
}

@end


@implementation PanoPoolService (Pubilc)

#pragma mark -- PanoTurnPageDelegate

- (void)fetchLastPage {
    if ([self enableFetchLastPage]) {
        _index-- ;
        [self notify];
        [self startAtiveAudio];
    }
}

- (void)fetchNextPage {
    if ([self enableFetchNextPage]) {
        _index++ ;
        [self notify];
        [self startAtiveAudio];
    }
}

- (void)switchToPage:(PageIndex)index {
    _index = index;
    [self notify];
}

- (BOOL)enableFetchLastPage {
    return _index > 0;
}

- (BOOL)enableFetchNextPage {
    if (_index == 0) {
        return [self isSharing] ? self.data.count >= 2 : self.data.count > 2;
    } else {
        return self.data.count > _index * 3 + 1;
    }
}
@end

