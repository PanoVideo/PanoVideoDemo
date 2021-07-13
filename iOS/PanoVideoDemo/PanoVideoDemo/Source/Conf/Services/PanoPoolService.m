//
//  PanoPoolService.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolService.h"
#import "PanoServiceManager.h"
#import "PanoUserService.h"
#import "PanoCallClient.h"



static NSUInteger PoolFirstPageMaxCount = 2;

static NSUInteger PoolOtherPageMaxCount = 4;

@interface PanoPoolService () <PanoUserDelegate, PanoAnnotationDelegate>{
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
@property (strong, nonatomic) PanoViewInstance *desktopInstance;
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
    _annotationService = [PanoAnnotationService new];
    _annotationService.delegate = self;
    _userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
    _userService.delegate = self;
    _enableRender = true;
    PanoRtcAnnotationManager *mgr=PanoCallClient.sharedInstance.engineKit.annotationManager;
    [mgr setDelegate:_annotationService];
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
- (void)notify {
    
    if (!_enableRender) {
        return;
    }
    if ([self.delegate respondsToSelector:@selector(onPoolMediaChanged:)]) {
        PanoViewPage *page = [[PanoViewPage alloc] init];
        if (_index == 0) {
            NSInteger len = _dataSource.count >= 2 ? PoolFirstPageMaxCount : _dataSource.count;
            NSArray * array = [_dataSource subarrayWithRange:NSMakeRange(0, len)];
            // 1. 查找到正在语音激励的用户
            PanoViewInstance *activeAudioInstance = nil;
            for (PanoViewInstance *instance in _dataSource) {
                if (instance.userId == _activeAudioUserID && instance.type == PanoViewInstance_Video) {
                    activeAudioInstance = instance;
                    break;
                }
            }
            // 如果谁开启了批注
            if (_activeAnnotation!= nil && _isAnnotating) {
                PanoViewInstance *annotation = [[PanoViewInstance alloc] initWithUserId:_activeAnnotation.userId type:_activeAnnotation.type];
                PanoViewInstance *other = nil;
                for (PanoViewInstance *temp in _dataSource) {
                    if (temp.type == PanoViewInstance_Video && ![temp isEqual:annotation]) {
                        other = temp;
                    }
                }
                page.instances = other ? @[annotation, other] : @[annotation];
            }
            else if (_pinInstance != nil) {
                page.instances = @[_pinInstance, _dataSource.firstObject];
            } else if (_desktopInstance) {
                page.instances = @[_desktopInstance, self.activeSpeakerUser];
            }
            // 2. 激励用户 生成对应的数组
            else if (_isActivingVoice && activeAudioInstance) {
                page.instances = @[activeAudioInstance, _dataSource.firstObject];
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
            [mArray addObject:_dataSource.firstObject];
            NSUInteger loc = 1 + (_index - 1) * (PoolOtherPageMaxCount - 1);
            NSUInteger len = _dataSource.count - loc > (PoolOtherPageMaxCount - 1) ? (PoolOtherPageMaxCount - 1) : _dataSource.count - loc;
            [mArray addObjectsFromArray:[_dataSource subarrayWithRange:NSMakeRange(loc, len)]];
            for (PanoViewInstance *instance in mArray) {
                instance.mode = PanoViewInstance_Avg;
            }
            page.instances = mArray;
            page.type = [self layoutType:mArray.count];
        }
//        NSLog(@"page-> %@", page);
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
        [_desktopInstances removeObject:instance];
        [_dataSource removeObject:instance];
        [self addDesktopInstance:instance];
        completion();
    } else if (instance.userId != _userService.me.userId && instance.type == PanoViewInstance_Video && _desktopInstances.count == 0) {
        // 如果这个实例类型是视频, 且没有桌面共享
        _pinInstance = instance;
        _index = 0;
        [self startRender];
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
    if (_dataSource.count == 2 && [self isSharing]) {
        return 2;
    }
    if (_dataSource.count <= 2) {
        return 1;
    }
    PageIndex result = (PageIndex)ceil((_dataSource.count - 1) / 3.0) + 1;
    return result;
}

/// 是否正在开启共享，是否开启了桌面或者白板
- (BOOL)isSharing {
    return _desktopInstance != nil;
}

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
        return [self isSharing] ? _dataSource.count >= 2 : _dataSource.count > 2;
    } else {
        return _dataSource.count > _index * 3 + 1;
    }
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
    [_dataSource removeObject:desktopInstance];
    [_desktopInstances removeObject:desktopInstance];
    [_dataSource removeObject:instance];
    while (_index >= [self numbersOfIndexs]) {
        _index--;
    }
    if (_desktopInstance.userId == user.userId) {
        _desktopInstance = nil;
    }
    if (user.userId == _activeAudioUserID) {
        _activeAudioUserID = 0;
    }
    if (_pinInstance && user.userId == _pinInstance.userId) {
        _pinInstance = nil;
    }
    if (_activeAnnotation.userId == user.userId) {
        [self onAnnotationStop:_activeAnnotation];
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
    
}

- (void)onUserDidEndShareingScreen:(PanoUserInfo *)user {
    PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:user.userId type:PanoViewInstance_Desktop];
    [_desktopInstances removeObject:desktopInstance];
    [_dataSource removeObject:desktopInstance];
    if ([_desktopInstance.user isEqual:user]) {
        _desktopInstance = nil;
    }
    if (_desktopInstances.count > 0) {
        _desktopInstance = [[PanoViewInstance alloc] initWithUserId:_desktopInstances.firstObject.userId type:PanoViewInstance_Desktop];
    }
    [self notify];
}

- (void)onUserDidBeginSharingScreen:(PanoUserInfo *)user {
    _pinInstance = nil;
    PanoViewInstance *desktopInstance = [[PanoViewInstance alloc] initWithUserId:user.userId type:PanoViewInstance_Desktop];
    [self addDesktopInstance:desktopInstance];
}

- (void)addDesktopInstance:(PanoViewInstance *)desktopInstance {
    if (![_desktopInstances containsObject:desktopInstance]) {
        [_desktopInstances insertObject:desktopInstance atIndex:0];
    }
    
    for (NSInteger i = 1; i<_desktopInstances.count; i++) {
        PanoViewInstance *tempInstance = _desktopInstances[i];
        if (![_dataSource containsObject:tempInstance]) {
            [_dataSource addObject:tempInstance];
        }
    }
    if (![_desktopInstance isEqual:desktopInstance]) {
        _desktopInstance = desktopInstance;
        _index = 0;
        [self startRender];
    }
}

#pragma mark -- PanoRtcEngineDelegate

#pragma mark -- PanoAnnotationDelegate
- (void)onAnnotationStart:(PanoAnnotationItem *)item {
    _isAnnotating = true;
    _activeAnnotation = item;
    [self switchToPage:0];
    [self notify];
    [_delegate onAnnotationStart:item];
}

- (void)onAnnotationStop:(PanoAnnotationItem *)item {
    _activeAnnotation = nil;
    _isAnnotating = false;
    [self notify];
    [_delegate onAnnotationStop:item];
}

- (void)startAnnotation {
    [self p_startAnnotation];
    [self switchToPage:0];
}

- (void)p_startAnnotation {
    if (self.activeAnnotation) {
        return;
    }
    PanoAnnotationItem *item = [[PanoAnnotationItem alloc] initWithUserId:PanoCallClient.sharedInstance.userId type:PanoViewInstance_Video];
    [self onAnnotationStart:item];
}

- (PanoAnnotationItem *)myVideoAnnotation {
    PanoAnnotationItem *myVideoItem = [PanoAnnotationItem new];
    myVideoItem.streamId = 0;
    myVideoItem.type = PanoViewInstance_Video;
    myVideoItem.userId = PanoCallClient.sharedInstance.userId;
    return myVideoItem;
}

- (void)stopAnnotation {
    if (_activeAnnotation) {
        [_annotationService hideAnnotationWithItem:_activeAnnotation];
        if ([_activeAnnotation isEqual:[self myVideoAnnotation]]) {
            [_annotationService stopAnnotationWithItem:_activeAnnotation];
        }
    }
} 


@end
