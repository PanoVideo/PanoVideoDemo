//
//  PanoPoolView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolView.h"
#import "PanoPoolService.h"
#import "PanoVideoView.h"
#import "PanoDesktopView.h"
#import "PanoWhiteboardView.h"
#import "PanoCallClient.h"
#import "PanoPoolFullScreenLayout.h"
#import "PanoPoolFloatLayout.h"
#import "PanoPoolFourAvgLayout.h"
#import "UIView+Extension.h"
#import "MBProgressHUD+Extension.h"
#import "UIColor+Extension.h"

@interface PanoPoolView ()
<PanoPoolDelegate,
 PanoRoleDelegate,
 PanoWhiteboardDelegate>
{
    NSMutableDictionary<PanoMediaInfoKey,id> *_layoutInfo;
}

@property (nonatomic, strong) id <PanoPoolLayoutDelegate> layoutState;

@property (nonatomic, weak) PanoPoolService *poolService;

@property (nonatomic, strong) NSMutableArray *prevMedias;

@property (nonatomic, strong) PanoViewPage *currentPage;

@property (nonatomic, strong) UIPanGestureRecognizer *panGestureRecognizer;

@property (nonatomic, assign) CGPoint initialPoint;

@property (nonatomic, strong) PanoWhiteboardView *wbView;

@end

@implementation PanoPoolView

- (void)initViews {
    [super initViews];
    _contentView = [[UIView alloc] init];
    _contentView.backgroundColor = [UIColor pano_colorWithHexString:@"111111"];
    [self addSubview:_contentView];
    
    UISwipeGestureRecognizer *leftSwipeGesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(fetchNextPage:)];
    leftSwipeGesture.direction = UISwipeGestureRecognizerDirectionLeft;
    [self addGestureRecognizer:leftSwipeGesture];
    
    UISwipeGestureRecognizer *rightSwipeGesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(fetchLastPage:)];
    rightSwipeGesture.direction = UISwipeGestureRecognizerDirectionRight;
    [self addGestureRecognizer:rightSwipeGesture];
    
    [self initService];
}

- (void)initService {
    [PanoCallClient.shared.wb addDelegate:self];
    _poolService = PanoCallClient.shared.pool;
    _poolService.delegate = self;
    [_poolService start];
    _prevMedias = [NSMutableArray array];
    _panGestureRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(dragView:)];
    _layoutInfo = [NSMutableDictionary dictionary];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(statusBarOrientationDidChange) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
}

- (void)statusBarOrientationDidChange {
    [self.layoutState layoutWithInfo:_layoutInfo];
}

- (PanoWhiteboardView *)wbView {
    if (!_wbView) {
        _wbView = [[PanoWhiteboardView alloc] init];
        _wbView.delegate = self;
    }
    return _wbView;
}

- (void)fetchNextPage:(id)gesture {
    [_poolService fetchNextPage];
}

- (void)fetchLastPage:(id)gesture {
    [_poolService fetchLastPage];
}

- (void)initConstraints {
    [super initConstraints];
    [_contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
}

- (NSArray<PanoBaseMediaView *> *)medias {
    return self.contentView.subviews;
}

- (void)updateVideoRenderConfig:(id __nullable)config {
    UIView *videoView = nil;
    for (PanoVideoView *view in self.contentView.subviews) {
        if (view.instance.userId == [PanoCallClient.shared.userMgr me].userId && view.instance.type == PanoViewInstance_Video) {
            videoView = view.contentView;
            break;
        }
    }
    [PanoCallClient.shared.video updateMyVideoWithView:videoView config:config];
}

- (void)updateMediaLayout:(NSDictionary<PanoMediaInfoKey,id> *)info {
    [_layoutInfo addEntriesFromDictionary:info];
    if (_panGestureRecognizer.view && _panGestureRecognizer.state == UIGestureRecognizerStatePossible &&
        info[PanoTopToolBarState]) {
        [self remakeRightCenter:self->_panGestureRecognizer.view];
    }
}

- (void)stopRender {
    [_poolService stopRender];
    [_prevMedias removeAllObjects];
    for (PanoBaseMediaView *media in self.contentView.subviews) {
        [media stop];
        [media removeFromSuperview];
    }
}

- (void)startRender {
    [_poolService startRender];
}

- (void)enableWhiteboard:(BOOL)enable {
    [self.gestureRecognizers enumerateObjectsUsingBlock:^(__kindof UIGestureRecognizer * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.enabled = !enable;
    }];
    if (enable) {
        [self.poolService switchToPage:0];
    }
    [self.wbView setEnable:enable];
}

- (void)onPoolMediaChanged:(PanoViewPage *)page {
    
    BOOL needLayout = ![_currentPage isEqual:page];
    
    _currentPage = page;
    
    if (page == nil) {
        return;
    }
    
    _mainUserId = page.instances.firstObject.userId;
    
    if ([self.delegate respondsToSelector:@selector(onPageIndexChanged:numbersOfIndexs:)]) {
        [self.delegate onPageIndexChanged:_poolService.currentIndex numbersOfIndexs:_poolService.numbersOfIndexs];
    }
    
    if (needLayout) {
        if ([self.delegate respondsToSelector:@selector(onPageTypeChanged:)]) {
            _layoutType = page.type;
            [self.delegate onPageTypeChanged:page.type];
        }
        
        id <PanoPoolLayoutDelegate> state = [self layoutWithState:page.type];
        self.layoutState = state;
    }
    
    // 1. 新的视频数组
    NSMutableArray *tempArray = [NSMutableArray array];
    for (PanoViewInstance *instance in page.instances) {
        PanoBaseMediaView *media = [self createMeidaView:instance];
        [tempArray addObject:media];
    }
    // 2. 停止订阅其它人的视频
    for (PanoBaseMediaView *media in _prevMedias) {
        if (![tempArray containsObject:media]) {
            [media stop];
            [media removeFromSuperview];
        }
    }
    
    // 3. 添加新的视频
    __weak typeof(self) weakSelf = self;
    for (PanoBaseMediaView *media in tempArray) {
        if (![self.contentView.subviews containsObject:media]) {
            [self.contentView addSubview:media];
        }
        BOOL enable = page.type == PanoViewPageLayout_4_Avg ||
                     (page.type == PanoViewPageLayout_Float && media.instance.mode == PanoViewInstance_Float) ||
                     (media.instance.mode == PanoViewInstance_Max && media.instance.type == PanoViewInstance_Video);
        media.enableDoubleClick = enable;
        // 双击事件回调
        media.doubleClickBlock = ^(PanoViewInstance * _Nonnull instance) {
            if (instance.mode == PanoViewInstance_Float) {
                // TODO 切换布局
               // [weakSelf.poolService switchInstance:instance];
            } else if (page.type == PanoViewPageLayout_4_Avg) {
                [weakSelf.poolService togglePinViewInstance:instance completion:^{
                    if (instance.type == PanoViewInstance_Video) {
                        [MBProgressHUD showMessage:NSLocalizedString(@"Pin Video Tip", nil) addedToView:self duration:2];
                    }
                }];
            } else if (instance.mode == PanoViewInstance_Max) {
                [weakSelf.poolService cancelPinViewInstance:instance completion:^{
                    [MBProgressHUD showMessage:NSLocalizedString(@"Exit Pin Video Tip", nil) addedToView:self duration:2];
                }];
            }
        };
        if (page.type == PanoViewPageLayout_Float && media.instance.mode == PanoViewInstance_Float) {
            if (![media.gestureRecognizers containsObject:_panGestureRecognizer]) {
                [_panGestureRecognizer.view removeGestureRecognizer:_panGestureRecognizer];
                [media addGestureRecognizer:_panGestureRecognizer];
            }
        } else {
            if ([media.gestureRecognizers containsObject:_panGestureRecognizer]) {
                [media removeGestureRecognizer:_panGestureRecognizer];
            }
        }
    }
    
    // 4. layout;
    if (needLayout) {
        [self.layoutState layoutWithViews:tempArray layoutInfo:_layoutInfo];
    }
    
    // 5. 缓存当前容器 其它人的视频
    [_prevMedias removeAllObjects];
    [_prevMedias addObjectsFromArray:tempArray];
    
    // 6. 开启渲染视频
    [tempArray makeObjectsPerformSelector:@selector(start)];
    
}

- (id <PanoPoolLayoutDelegate>)layoutWithState:(PanoViewPageLayoutType)layoutType {
    id <PanoPoolLayoutDelegate> state = nil;
    switch (layoutType) {
        case PanoViewPageLayout_FullScreen:
            state = [[PanoPoolFullScreenLayout alloc] init];
            break;
        case PanoViewPageLayout_Float:
            state = [[PanoPoolFloatLayout alloc] init];
            break;
        case PanoViewPageLayout_4_Avg:
            state = [[PanoPoolFourAvgLayout alloc] init];
            break;
        default:
            break;
    }
    return state;
}

- (PanoBaseMediaView *)createMeidaView:(PanoViewInstance *)instance {
    for (PanoBaseMediaView *meida in self.contentView.subviews) {
        if ([meida.instance isEqual:instance]) {
            meida.instance = instance;
            return meida;
        }
    }
    PanoBaseMediaView *mediaView = nil;
    switch (instance.type) {
        case PanoViewInstance_Video:
            mediaView = [[PanoVideoView alloc] init];
            break;
        case PanoViewInstance_Desktop:
            mediaView = [[PanoDesktopView alloc] init];
            break;
        case PanoViewInstance_Whiteboard:
            mediaView =  self.wbView;
            break;
        default:
            break;
    }
    mediaView.instance = instance;
    return mediaView;
}

- (void)onAudioActiveUserChanged:(PanoViewInstance *)instance activing:(BOOL)activing {
    BOOL flag = [self.layoutState isKindOfClass:[PanoPoolFourAvgLayout class]];
    for (PanoBaseMediaView *meida in self.contentView.subviews) {
        if (instance && [meida.instance isEqual:instance] && flag) {
            meida.active = activing;
        } else {
            meida.active = false;
        }
    }
    if ([self.delegate respondsToSelector:@selector(onAudioActiveUserChanged:activing:)]) {
        [self.delegate onAudioActiveUserChanged:instance activing:activing];
    }
}

- (void)onMyAudioActiveChanged:(BOOL)activing {
    if ([self.delegate respondsToSelector:@selector(onMyAudioActiveChanged:)]) {
        [self.delegate onMyAudioActiveChanged:activing];
    }
}

- (void)onSpeakingWhenTheAudioMuted {
    [self.delegate onSpeakingWhenTheAudioMuted];
}

- (void)dragView:(UIPanGestureRecognizer *)gestureRecognizer{
    if (self.currentPage.type != PanoViewPageLayout_Float) {
        return;
    }
    UIView *view = gestureRecognizer.view;
    CGPoint p = [gestureRecognizer translationInView:self.superview];
    NSLog(@"gestureRecognizer.state-->%ld", (long)gestureRecognizer.state);
    switch (gestureRecognizer.state) {
        case UIGestureRecognizerStateBegan:
            _initialPoint = view.center;
            break;
        case UIGestureRecognizerStateChanged:
            view.center = CGPointMake(_initialPoint.x + p.x, _initialPoint.y + p.y);
            [_layoutInfo setObject:[NSValue valueWithCGPoint:view.center] forKey:PanoMediaViewPostionKey];
            break;
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateFailed:
        case UIGestureRecognizerStateEnded:
            [self remakeRightCenter:view];
            break;
        default:
            break;
    }
}

- (void)remakeRightCenter:(UIView *)view {
    NSLog(@"remakeRightCenter");
    [UIView animateWithDuration:0.25 animations:^{
        [self remakeRightCenter2:view];
    } completion:^(BOOL finished) {
    }];
}

- (void)remakeRightCenter2:(UIView *)view{
    if (self.currentPage.type != PanoViewPageLayout_Float) {
        return;
    }
    NSLog(@"remakeRightCenter2 %@",view);
    CGPoint pointCurrent = view.center;
    CGFloat pX = 0;
    CGFloat pY = 0;
    CGFloat offset = 120;
    CGSize littleVideoSize = CGSizeMake(117.5, 157.5);
    BOOL topToolBarHidden = false;
    if (_layoutInfo) {
        topToolBarHidden = [_layoutInfo[PanoTopToolBarState] boolValue];
    }
    CGFloat top = (!topToolBarHidden ? (isIphoneX() ? 88 : 64) : (isIphoneX() ? 44 : 20)) + 7.5;
    CGFloat bottom = topToolBarHidden ?  (isIphoneX() ? 25 : 7.5 ) : 87.5;
    UIEdgeInsets edgeInsets = UIEdgeInsetsMake(top, 7.5, bottom, 7.5);
    if (pointCurrent.x <= littleVideoSize.width/2 + offset) {
        pX =  littleVideoSize.width/2 + edgeInsets.left;
    }else if(pointCurrent.x >= (CGRectGetWidth([UIScreen mainScreen].bounds) - littleVideoSize.width/2 - offset)){
        pX =(CGRectGetWidth([UIScreen mainScreen].bounds) - littleVideoSize.width/2) - edgeInsets.right;
    }else{
        pX = pointCurrent.x;
    }
    
    if (pointCurrent.y <=  littleVideoSize.height/2 + offset + edgeInsets.top) {
        pY =  littleVideoSize.height/2 + edgeInsets.top;
    }else if (pointCurrent.y >= (CGRectGetHeight([UIScreen mainScreen].bounds) - littleVideoSize.height/2 - offset -edgeInsets.bottom) ) {
        pY = (CGRectGetHeight([UIScreen mainScreen].bounds) - littleVideoSize.height/2)-edgeInsets.bottom;
    }else{
        pY = pointCurrent.y;
    }
    view.center = CGPointMake(pX, pY);
    [_layoutInfo setObject:[NSValue valueWithCGPoint:view.center] forKey:PanoMediaViewPostionKey];
    
    CGFloat x = view.frame.origin.x;
    CGFloat y = view.frame.origin.y;
    CGRect superViewFrame = self.frame;
    CGFloat x1 = superViewFrame.size.width - CGRectGetWidth(view.frame) - x;
    CGFloat y1 = superViewFrame.size.height -  CGRectGetHeight(view.frame) - y;
    UIEdgeInsets insets = UIEdgeInsetsZero;
    if (fabs(x) < fabs(x1)) {
        insets.left = fabs(x);
    } else {
        insets.right = fabs(x1);
    }
    if (fabs(y) < fabs(y1)) {
        insets.top = fabs(y);
    } else {
        insets.bottom = fabs(y1);
    }
    [_layoutInfo setObject:[NSValue valueWithUIEdgeInsets:insets] forKey:PanoFloatViewPostionKey];
    [self.layoutState layoutWithInfo:_layoutInfo];
}

#pragma mark -- PanoRoleDelegate
- (void)onMyRoleBecomeViewer {
    [self enableWhiteboard:false];
    [self.delegate onMyRoleBecomeViewer];
}

#pragma mark -- PanoWhiteboardDelegate
- (void)onWhiteboardStatusChanged:(BOOL)on {
    [self enableWhiteboard:on];
}

- (void)dealloc {
    [PanoCallClient.shared.wb removeDelegate:self];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
