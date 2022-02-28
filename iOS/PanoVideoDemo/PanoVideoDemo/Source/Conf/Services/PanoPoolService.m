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

static NSUInteger PoolOtherPageMaxCount = 4;

@interface PanoPoolService () <PanoUserDelegate, PanoWhiteboardDelegate>{
    __weak PanoUserService *_userService;
    PageIndex _index;
    NSMutableArray *_dataSource;
}
@property (assign, nonatomic) BOOL isSwitchFlag;
@property (strong, nonatomic) NSDate *lastSpeakDate;
@property (assign, nonatomic) NSInteger activeAudioUserID;
@property (strong, nonatomic) PanoViewPage *currentPage;
@property (strong, nonatomic) NSMutableArray<NSNumber *> *myActiveAudios;
@end

@implementation PanoPoolService

- (void)start {
    _index = 0;
    _dataSource = [NSMutableArray array];
    _myActiveAudios = [NSMutableArray array];
    _enableRender = true;
    _userService = PanoCallClient.shared.userMgr;
}

- (void)stopRender{
    _enableRender = false;
    [self.delegate onPoolMediaChanged:[[PanoViewPage alloc] init]];
}

- (void)startRender {
    _enableRender = true;
    [self notify];
}

- (void)setDelegate:(id<PanoPoolDelegate>)delegate {
    if (delegate) {
        [self addDelegate:delegate];
    } else {
        [self removeDelegate:_delegate];
    }
    _delegate = delegate;
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
        return PanoViewPageLayout_Float; // 演讲者模式显示 浮动布局
    } else if (len == 3 || len == 4 || (len == 2 && _index > 0)) {
        return PanoViewPageLayout_4_Avg; // 画廊模式显示 平均布局
    } else {
        NSLog(@"NOT SUPPORT");
        return PanoViewPageLayout_FullScreen;
    }
}

- (NSArray<PanoViewInstance *> *)data {
    NSMutableArray *mArray = [NSMutableArray array];
    [mArray addObjectsFromArray:_dataSource];
    // 添加所有的桌面共享实例 && 移除Pin的桌面共享实例
    NSArray *desktopInstances = [self allDesktopInstances];
    [mArray addObjectsFromArray:desktopInstances];
    if (_pinInstance && _pinInstance.type == PanoViewInstance_Desktop) {
        [mArray removeObject:_pinInstance];
    } else if (desktopInstances.firstObject) {
        [mArray removeObject:desktopInstances.firstObject];
    }
    return mArray;
}

/// 优先级最高的桌面共享实例
- (PanoViewInstance *)desktopInstance {
    if (_pinInstance && _pinInstance.type == PanoViewInstance_Desktop) {
        return _pinInstance;
    }
    return [self allDesktopInstances].firstObject;
}

/// 其它所有的桌面共享实例
- (NSArray *)allDesktopInstances {
    return [[PanoCallClient.shared.userMgr allScreens] arrayByMappingObjectsUsingBlock:^id _Nullable(NSNumber * _Nonnull obj) {
        PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:[obj unsignedIntegerValue] type:PanoViewInstance_Desktop];
        return desktopInstance;
    }];
}

/// 主页面显示的数据源
- (NSArray<PanoViewInstance *> *)mainPageData {
    if ([PanoCallClient.shared.wb isOn]) {
        PanoViewInstance *instance = [[PanoViewInstance alloc] initWithUserId:0 type:PanoViewInstance_Whiteboard];
        return @[instance, self.activeSpeakerUser];
    }
    else if (self.desktopInstance != nil) {
        return @[self.desktopInstance, self.activeSpeakerUser];
    } else if (_pinInstance != nil) {
        return @[_pinInstance, self.data.firstObject];
    } else  {
        BOOL flag = self.activeSpeakerUser.userId != PanoCallClient.shared.userId;
        return flag ? @[self.activeSpeakerUser, self.data.firstObject] :   @[self.activeSpeakerUser];
    }
}

- (void)notify {
    if (!_enableRender) {
        return;
    }
    if ([self.delegate respondsToSelector:@selector(onPoolMediaChanged:)]) {
        PanoViewPage *page = [[PanoViewPage alloc] init];
        if (_index == 0) {
            page.instances = [self mainPageData];
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
    if (instance.userId != _userService.me.userId && ![self isSharing]) {
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
    return self.desktopInstance != nil ||
          [PanoCallClient.shared.wb isOn];
}

#pragma mark -- PanoUserDelegate

- (void)onUserAdded:(nonnull PanoUserInfo *)user {
    PanoViewInstance *instance = [self videoInstanceWithUser:user];
    [_dataSource addObject:instance];
    [self notify];
}

- (void)onUserRemoved:(nonnull PanoUserInfo *)user {
    PanoViewInstance *instance = [self videoInstanceWithUser:user];
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
        [self invokeWithAction:@selector(onAudioActiveUserStatusChanged:) completion:^(id  _Nonnull del) {
            [del onAudioActiveUserStatusChanged:instance];
        }];
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
    if (_pinInstance && user.userId == _pinInstance.userId)  {
        _pinInstance = nil;
    }
    [self updatePageIndex];
    [self notify];
}

- (void)onUserDidBeginSharingScreen:(PanoUserInfo *)user {
    _pinInstance = nil;
    _index = 0;
    [self notify];
}

- (void)updatePageIndex {
    while (_index >= [self numbersOfIndexs]) {
        _index--;
    }
}

#pragma mark -- PanoWhiteboardDelegate
- (void)onWhiteboardStatusChanged:(BOOL)on {
    if (on) {
        [PanoCallClient.shared.screen stopShare];
        [self switchToPage:0];
    }
    self.isAnnotationing = on;
    [self notify];
}

#pragma mark -- PanoRtcEngineDelegate
/**
 - ASL声音最大的那个
 - 如果是自己，则显声音第二大的那个, 一次类推
 - 如果ASL为空，则保留上一个active video
 - 如果active video离会了，则显声音第二大 或者第三大的那个， 如果ASL中没有，那么显示最早加入Room的用户
 - 如果只有自己，则为自己
 */
- (PanoViewInstance *)activeSpeakerUser {
    PanoViewInstance *instance = [[PanoViewInstance alloc] init];
    instance.type = PanoViewInstance_Video;
    BOOL isFound = false;
    NSUInteger index = [self.data indexOfObjectPassingTest:^BOOL(PanoViewInstance * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        return obj.userId == self.activeAudioUserID;
    }];
    if (index != NSNotFound) {
        instance.userId = _activeAudioUserID;
    } else {
        for (PanoViewInstance *temp in self.data) {
            if (temp.userId != PanoCallClient.shared.userId) {
                instance = temp;
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            instance.userId = PanoCallClient.shared.userId;
        }
    }
    return  instance;
}

@end


@implementation PanoPoolService (Pubilc)

#pragma mark -- PanoTurnPageDelegate

- (void)fetchLastPage {
    if ([self enableFetchLastPage]) {
        _index-- ;
        [self notify];
    }
}

- (void)fetchNextPage {
    if ([self enableFetchNextPage]) {
        _index++ ;
        [self notify];
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

