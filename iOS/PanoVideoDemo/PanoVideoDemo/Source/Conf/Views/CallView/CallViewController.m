//
//  CallViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "CallViewController.h"
#import "WhiteboardViewController.h"
#import "PanoUserListViewController.h"
#import "PanoCallClient.h"
#import "PanoPoolView.h"
#import "PanoVideoView.h"
#import "Reachability.h"
#import "MBProgressHUD+Extension.h"
#import "PanoAnnotationTool.h"

typedef NS_ENUM(NSInteger, AsyncAlertType) {
    kAsyncAlertNone = 0,
    kAsyncAlertFetchFail,
    kAsyncAlertJoinFail,
    kAsyncAlertExit,
    kAsyncAlertOpenWhiteboard,
};

static NSTimeInterval kCountDownTimeInterval = 1.0;
static NSTimeInterval kHienToolbarsTimeInterval = 6.0;
static NSTimeInterval kDelayDismissAlertTime = 3.0;

// KVO observer keys
static NSString *kResolutionObserverKey = @"resolution";

@interface CallViewController () <PanoRtcEngineDelegate, PanoRtcWhiteboardDelegate, WhiteboardViewDelegate, PanoPoolViewDelegate, PanoAnnotationToolDelegate, PanoWhiteboardDelegate>

@property (strong, nonatomic) IBOutlet UIView * topToolbarView;
@property (strong, nonatomic) IBOutlet UIButton * speakerButton;
@property (strong, nonatomic) IBOutlet UILabel * roomId;
@property (strong, nonatomic) IBOutlet UILabel * countDown;

@property (strong, nonatomic) IBOutlet UIView * bottomToolbarView;
@property (strong, nonatomic) IBOutlet UIButton * audioButton;
@property (strong, nonatomic) IBOutlet UIButton * videoButton;
@property (strong, nonatomic) IBOutlet UIButton * whiteboardButton;
@property (strong, nonatomic) IBOutlet UIButton * settingButton;
@property (weak, nonatomic) IBOutlet UIButton *userListButton;
@property (strong, nonatomic) IBOutlet UIPageControl * layoutControl;

@property (strong, nonatomic) IBOutlet UIActivityIndicatorView * joinIndicator;
@property (weak, nonatomic) IBOutlet UIButton *exitButton;
@property (weak, nonatomic) IBOutlet UIButton *switchCameraBtn;
@property (assign, nonatomic) BOOL speakerStatus;

@property (weak, nonatomic) WhiteboardViewController * whiteboardViewController;
@property (assign, nonatomic) BOOL whiteboardOpened;

@property (strong, nonatomic) NSTimer * countDownTimer;
@property (assign, nonatomic) UInt32 remainTime;

@property (strong, nonatomic) NSTimer * hideToolbarsTimer;

@property (assign, nonatomic) BOOL callLeft;

@property (assign, nonatomic) BOOL viewAppeared;
@property (assign, nonatomic) AsyncAlertType asyncAlert;

@property (assign, nonatomic) UInt64 backMainUserId;

@property (strong, nonatomic) PanoPoolView *poolView;
@property (weak, nonatomic) PanoVideoView *wbVideoView;
@property (strong, nonatomic) Reachability *reachability;
@property (strong, nonatomic) PanoAnnotationTool* annotationTool;
@property (assign, nonatomic) CGPoint initialPoint;
@property (weak, nonatomic) IBOutlet UIButton *backButton;
@property (weak, nonatomic) PanoConfig *config;

@property (assign, nonatomic) BOOL shownMuteTip;
@property (weak, nonatomic) IBOutlet UILabel *infoLabel;
@property (weak, nonatomic) PanoUserService *userService;
@property (strong, nonatomic) MBProgressHUD *reconnectingHUD;
@end

@implementation CallViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _config = [PanoCallClient shared].config;
    _userService = PanoCallClient.shared.userMgr;
    [self setLocalizable];
    [self initToolbars];
    [self initLayout];
    [self initGestureRecognizers];
    [self initWhiteboard];
    [self hideCountDown];
    [self initAsyncAlert];
    [self addObservers];
    [self joinCall];
    [UIApplication.sharedApplication setIdleTimerDisabled:true];
    _reachability = [Reachability reachabilityWithHostname:@"pano.video.call"];
    [_reachability startNotifier];
    __weak typeof(self) weakSelf = self;
    [_reachability setReachabilityBlock:^(Reachability *reachability, SCNetworkConnectionFlags flags) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (reachability.currentReachabilityStatus == ReachableViaWWAN) {
                [MBProgressHUD showMessage:NSLocalizedString(@"您正在使用数据流量...", nil) addedToView:weakSelf.view duration:1];
            }
        });
    }];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    self.viewAppeared = YES;
    [self checkAsyncAlert];
}

- (void)viewWillDisappear:(BOOL)animated {
    self.viewAppeared = NO;
    [super viewWillDisappear:animated];
}

- (IBAction)clickExit:(id)sender {
    if (PanoCallClient.shared.leaveConfirm) {
        [self presentExitConfirm];
    } else {
        [self leaveCall];
    }
}

- (IBAction)switchSpeaker:(id)sender {
    [self enableSpeaker:!self.speakerStatus];
}

- (IBAction)switchCamera:(id)sender {
    [PanoCallClient.shared.video switchCamera];
    [self.poolView updateVideoRenderConfig:nil];
}

- (IBAction)clickAudio:(id)sender {
    [self muteAudio:!_userService.me.isAudioMuted];
}

- (IBAction)clickVideo:(id)sender {
    [self enableVideo:_userService.me.isVideoMuted];
}

- (IBAction)showMoreAction:(UIButton *)sender {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:nil message:nil
                                                             preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil)
                                                  style:UIAlertActionStyleCancel
                                                handler:nil];
    [alert addAction:cancel];
    UIAlertAction *setting = [UIAlertAction actionWithTitle:NSLocalizedString(@"设置", nil)
                                                  style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self openSettingPage];
    }];
    [alert addAction:setting];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        alert.popoverPresentationController.sourceView = sender;
        alert.popoverPresentationController.sourceRect = sender.bounds;
    }
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showCallAlertWith:(NSString *)title {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:title
                                                                    message:NSLocalizedString(@"ContactCustomer", nil)
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"Call", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"tel://18989880717"]];
    }];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"cancel", nil)
                                                      style:UIAlertActionStyleDefault
                                                    handler:nil];
    [alert addAction:cancel];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

- (IBAction)showShareAlert:(UIButton *)sender {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:nil message:nil
                                                             preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil)
                                                  style:UIAlertActionStyleCancel
                                                handler:nil];
    [alert addAction:cancel];
    UIAlertAction *annotation = [UIAlertAction actionWithTitle:NSLocalizedString(@"共享白板", nil)
                                                  style:UIAlertActionStyleDefault
                                                       handler:^(UIAlertAction * _Nonnull action) {
        [self openWhiteboard];
    }];
    [alert addAction:annotation];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        alert.popoverPresentationController.sourceView = sender;
        alert.popoverPresentationController.sourceRect = sender.bounds;
    }
    [self presentViewController:alert animated:YES completion:nil];
}

- (IBAction)backButtonAction:(id)sender {
   
}

- (void)dismissPresentedController {
    if (self.presentedViewController) {
        [self.presentedViewController dismissViewControllerAnimated:true completion:nil];
    }
}

- (void)openSettingPage {
    UIViewController *settingVc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"setting"];
    [self presentViewController:settingVc animated:true completion:nil];
}

#pragma mark - Navigation

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(nullable id)sender {
    BOOL perform = NO;
    if ([identifier compare:@"Whiteboard"] == NSOrderedSame) {
        if (PanoCallClient.shared.host || self.whiteboardOpened) {
            perform = YES;
        } else {
            [self presentAlert:NSLocalizedString(@"whiteboardOff", nil)];
        }
    } else {
        perform = YES;
    }
    return perform;
}

#pragma mark - PanoRtcEngineDelegate

- (void)onChannelJoinConfirm:(PanoResult)result {
    [self displayMessage:[NSString stringWithFormat:@"Join channel confirm with %ld", (long)result]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.joinIndicator stopAnimating];
        if (kPanoResultOK == result) {
            NSString *identifier = [NSString stringWithFormat:@"%@_%@", PanoCallClient.shared.roomId,PanoCallClient.shared.userName];
            [self checkAutoStart];
            PanoDesktopService *desktopService = PanoCallClient.shared.screen;
            [desktopService start];
        } else {
            if (self.viewAppeared) {
                [self presentAlert:NSLocalizedString(@"joinFail", nil)];
            } else {
                self.asyncAlert = kAsyncAlertJoinFail;
            }
        }
    });
}

- (void)onChannelLeaveIndication:(PanoResult)result {
    [self displayMessage:[NSString stringWithFormat:@"Leave channel indication with %ld", (long)result]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication.sharedApplication setIdleTimerDisabled:false];
        [self.reconnectingHUD hideAnimated:true];
        [self closeWhiteboard];
        [self hideCountDown];
        [self resetLayout];
        [self resetToolbars];
        if (self.viewAppeared) {
            [self presentExitAlert];
        } else {
            self.asyncAlert = kAsyncAlertExit;
        }
        [self leaveCall];
    });
}

- (void)onChannelFailover:(PanoFailoverState)state {
    [self displayMessage:[NSString stringWithFormat:@"Channel failover with %ld", (long)state]];
    if (state == kPanoFailoverReconnecting) {
        self.reconnectingHUD = [MBProgressHUD showIndicatorViewWithMessage:NSLocalizedString(@"正在重连...", nil) addedToView:self.view duration:-1];
    } else {
        [self.reconnectingHUD hideAnimated:true];
    }
    
}

- (void)onChannelCountDown:(UInt32)remain {
    [self displayMessage:[NSString stringWithFormat:@"Channel count down with %u seconds", (unsigned)remain]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self showCountDown:remain];
    });
}

- (void)onUserJoinIndication:(UInt64)userId withName:(NSString * _Nullable)userName {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu (%@) join channel", (unsigned long)userId, userName]];
    dispatch_async(dispatch_get_main_queue(), ^{
        //[self addRemoteUser:userId withName:userName];
    });
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu leave room with %ld", (unsigned long)userId, (long)reason]];
    dispatch_async(dispatch_get_main_queue(), ^{
        //[self removeRemoteUser:userId];
    });
}

- (void)onUserAudioStart:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu start audio", (unsigned long)userId]];
}

- (void)onUserAudioStop:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu stop audio", (unsigned long)userId]];
}

- (void)onUserAudioMute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu mute audio", (unsigned long)userId]];
}

- (void)onUserAudioUnmute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu unmute audio", (unsigned long)userId]];
}

- (void)onUserVideoStart:(UInt64)userId
          withMaxProfile:(PanoVideoProfileType)maxProfile {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu start video with max profile %ld", (unsigned long)userId, (long)maxProfile]];
}

- (void)onUserVideoStop:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu stop video", (unsigned long)userId]];
}

- (void)onUserVideoMute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu mute video", (unsigned long)userId]];
}

- (void)onUserVideoUnmute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu unmute video", (unsigned long)userId]];
}

- (void)onUserScreenStart:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu start screen", (unsigned long)userId]];
}

- (void)onUserScreenStop:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu stop screen", (unsigned long)userId]];
}

- (void)onUserScreenMute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu mute screen", (unsigned long)userId]];
}

- (void)onUserScreenUnmute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu unmute screen", (unsigned long)userId]];
}

- (void)onUserAudioSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu audio subscribe result %ld", (unsigned long)userId, (long)result]];
}

- (void)onUserVideoSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu video subscribe result %ld", (unsigned long)userId, (long)result]];
}

- (void)onUserScreenSubscribe:(UInt64)userId
                  withResult:(PanoSubscribeResult)result {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu screen subscribe result %ld", (unsigned long)userId, (long)result]];
}

- (void)onFirstAudioDataReceived:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu first audio data received", (unsigned long)userId]];
}

- (void)onFirstVideoDataReceived:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu first video data received", (unsigned long)userId]];
}

- (void)onFirstScreenDataReceived:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu first screen data received", (unsigned long)userId]];
}

- (void)onAudioSendStats:(PanoRtcAudioSendStats * _Nonnull)stats {
}

- (void)onAudioRecvStats:(PanoRtcAudioRecvStats * _Nonnull)stats {
}

- (void)onVideoSendStats:(PanoRtcVideoSendStats * _Nonnull)stats {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (PanoCallClient.shared.userId == self.poolView.mainUserId) {
            [self onMainUserStatisticsChanged:@{@"video": [self videoInfoDescription:stats]}];
        }
    });
}

- (void)onVideoRecvStats:(PanoRtcVideoRecvStats * _Nonnull)stats {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (stats.userId == self.poolView.mainUserId) {
            [self onMainUserStatisticsChanged:@{@"video": [self videoInfoDescription:stats]}];
        }
    });
}

- (void)onScreenSendStats:(PanoRtcScreenSendStats * _Nonnull)stats {
}

- (void)onScreenRecvStats:(PanoRtcScreenRecvStats * _Nonnull)stats {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (stats.userId == self.poolView.mainUserId) {
            [self onMainUserStatisticsChanged:@{@"screen": [self videoInfoDescription:stats]}];
        }
    });
}

- (void)onVideoSendBweStats:(PanoRtcVideoSendBweStats * _Nonnull)stats {
}

- (void)onVideoRecvBweStats:(PanoRtcVideoRecvBweStats * _Nonnull)stats {
}

- (void)onSystemStats:(PanoRtcSystemStats * _Nonnull)stats {
}

- (void)onWhiteboardAvailable {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard available"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [PanoCallClient.shared.wb addDelegate:self];
    });
}

- (void)onWhiteboardUnavailable {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard unavailable"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [PanoCallClient.shared.engineKit.whiteboardEngine setDelegate:nil];
        [self closeWhiteboard];
    });
}

- (void)onWhiteboardStart {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard start"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self openWhiteboard];
    });
}

- (void)onWhiteboardStop {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard stop"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self closeWhiteboard];
//        [PanoCallClient.shared.engineKit.whiteboardEngine setDelegate:self];
    });
}

- (void)onNetworkQuality:(PanoQualityRating)quality withUser:(UInt64)userId {
    //NSLog(@"onNetworkQuality->%llu %ld",userId, quality);
    for (PanoBaseMediaView *mediaView in self.poolView.medias) {
        if (mediaView.instance.userId == userId) {
            [mediaView update:@{PanoNetworkStatus : @(quality)}];
            break;
        }
    }
}

#pragma mark - PanoRtcWhiteboardDelegate

- (void)onRoleTypeChanged:(PanoWBRoleType)newRole {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard role type is changed to %ld", (long)newRole]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateWhiteboardRole:newRole];
    });
}

- (void)onContentUpdated {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard content is updated"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateWhiteboard];
    });
}

#pragma mark - WhiteboardViewDelegate
- (void)onVisionShareStarted:(UInt64)userId {
    PanoRtcWhiteboard *wb = PanoCallClient.shared.engineKit.whiteboardEngine;
    PanoUserInfo *info = [_userService findUserWithId:userId];
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"'%@'开启了视角跟随", nil), info.userName] ;
    [self showToast:msg];
    [wb startFollowVision];
}

- (void)onVisionShareStopped:(UInt64)userId {
    PanoUserInfo *info = [_userService findUserWithId:userId];
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"'%@'关闭了视觉跟随", nil), info.userName] ;
    [self showToast:msg];
    PanoRtcWhiteboard *wb = PanoCallClient.shared.engineKit.whiteboardEngine;
    [wb stopFollowVision];
}

- (void)whiteboardViewDidOpen {
    // [self switchToWhiteboard];
    [_poolView stopRender];
    PanoVideoView *videoView = [[PanoVideoView alloc] init];
    videoView.hidden = true;
    [self.whiteboardViewController.view addSubview:videoView];
    [self.whiteboardViewController.view bringSubviewToFront:videoView];
    
    _wbVideoView = videoView;
    UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
    [_wbVideoView addGestureRecognizer:pan];
    videoView.frame = CGRectMake(self.view.bounds.size.width-117.5-15, 88, 117.5, 157.5);
    // 如果已经有激励用户，那么直接显示激励用户
    PanoPoolService *poolService = PanoCallClient.shared.pool;
    PanoViewInstance *instance = poolService.activeSpeakerUser;
    instance.mode = PanoViewInstance_Float;
    _wbVideoView.instance = instance;
    _wbVideoView.hidden = false;
    [_wbVideoView start];
}

-(void)handlePan:(UIPanGestureRecognizer*)gestureRecognizer{
    UIView *view = gestureRecognizer.view;
    CGPoint p = [gestureRecognizer translationInView:self.view];
    switch (gestureRecognizer.state) {
        case UIGestureRecognizerStatePossible:
        case UIGestureRecognizerStateBegan:
            _initialPoint = view.center;
            break;
        case UIGestureRecognizerStateChanged:
            view.center = CGPointMake(_initialPoint.x + p.x, _initialPoint.y + p.y);
            break;
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateFailed:
        case UIGestureRecognizerStateEnded:
            [UIView animateWithDuration:0.25 animations:^{
                [self remakeRightCenter:view];
            }];
            break;
    }
}

- (void)remakeRightCenter:(UIView *)view{
    if (!view) {
        return;
    }
    CGPoint pointCurrent = view.center;
    CGFloat pX = 0;
    CGFloat pY = 0;
    CGFloat offset = 120;
    CGSize littleVideoSize = CGSizeMake(117.5, 157.5);
    CGFloat top =(isIphoneX() ? 44 : 20) + 50;
    CGFloat bottom = (isIphoneX() ? 25 : 7.5) + 50;
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
}


- (void)whiteboardViewWillClose {
    [_wbVideoView stop];
    [_wbVideoView removeFromSuperview];
    _wbVideoView = nil;
    [_poolView startRender];
    self.whiteboardOpened = NO;
}

- (void)whiteboardViewDidClose {
//    [PanoCallClient.shared.engineKit.whiteboardEngine setDelegate:self];
}

- (MBProgressHUD *)showToast:(NSString*)msg {
    UIView *view = self.view;
    if (self.presentedViewController) {
        view = self.presentedViewController.view;
    }
    return [MBProgressHUD showMessage:msg addedToView:view duration:2.0];
}

#pragma mark - Alert

- (void)presentAlert:(NSString *)message {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@""
                                                                    message:message
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:nil];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
    [self performSelector:@selector(dismissAlert:) withObject:alert afterDelay:kDelayDismissAlertTime];
}

- (void)dismissAlert:(UIAlertController *)alert{
    [alert dismissViewControllerAnimated:YES completion:nil];
}

- (void)presentExitAlert {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@""
                                                                    message:NSLocalizedString(@"exitAlert", nil)
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [self leaveCall];
    }];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)presentExitConfirm {
#define HideCloseChanel 1
#if HideCloseChanel
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@""
                                                                    message:NSLocalizedString(@"exitConfirm", nil)
                                                             preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"离开会议", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [self leaveCall];
    }];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"cancel", nil)
                                                      style:UIAlertActionStyleCancel
                                                    handler:nil];
    [alert addAction:cancel];
    [alert addAction:ok];
    if (isiPad()) {
        alert.popoverPresentationController.sourceView = self.exitButton;
        alert.popoverPresentationController.sourceRect = self.exitButton.bounds;
    }
    [self presentViewController:alert animated:YES completion:nil];
#else
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:nil
                                                                    message:NSLocalizedString(@"exitConfirm", nil)
                                                             preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"Leave Meeting", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [self leaveCall];
    }];
    
    UIAlertAction *endMeeting = [UIAlertAction actionWithTitle:NSLocalizedString(@"End Meeting", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [PanoCallClient.shared closeChannelWithHandler:^(NSError * _Nonnull error) {
            NSLog(@"error->%@", error);
        }];
        [self leaveCall];
    }];
    
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"cancel", nil)
                                                      style:UIAlertActionStyleCancel
                                                    handler:nil];
    [alert addAction:cancel];
    [alert addAction:ok];
    [alert addAction:endMeeting];
    if (isiPad()) {
        alert.popoverPresentationController.sourceView = self.exitButton;
        alert.popoverPresentationController.sourceRect = self.exitButton.bounds;
    }
    [self presentViewController:alert animated:YES completion:nil];
#endif
}

#pragma mark - Private

- (void)displayMessage:(NSString *)message {
#if defined(DEBUG)
    NSLog(@"%@", message);
#endif
}

- (void)setLocalizable {
    self.roomId.text = PanoCallClient.shared.roomId;
}

- (void)initLayout {
    _poolView = [[PanoPoolView alloc] init];
    _poolView.delegate = self;

    [self.view insertSubview:_poolView belowSubview:self.layoutControl];
    
    [_poolView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.view);
    }];
    
    [self resetLayout];
}

- (void)resetLayout {
    self.layoutControl.currentPage = 0;
}

- (void)initGestureRecognizers {
    // handle single tap gesture recognizers for toolbar hidden
    UITapGestureRecognizer *singleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchToolbarsHidden:)];
    singleTapGesture.numberOfTapsRequired = 1;
    singleTapGesture.numberOfTouchesRequired  = 1;
    [self.view addGestureRecognizer:singleTapGesture];
}

- (void)dismissToRootViewController {
    UIViewController * viewController = self.presentingViewController;
    while (viewController.presentingViewController) {
        viewController = viewController.presentingViewController;
    }
    [viewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)initToolbars {
    [self setButtonImageAndTitleEdgeInsets:self.audioButton];
    [self setButtonImageAndTitleEdgeInsets:self.videoButton];
    [self setButtonImageAndTitleEdgeInsets:self.whiteboardButton];
    [self setButtonImageAndTitleEdgeInsets:self.settingButton];
    [self setButtonImageAndTitleEdgeInsets:self.userListButton];
    [self enableSpeaker:PanoCallClient.shared.autoSpeaker];
    [self resetToolbars];
    [self hideToolbars:NO];
    [self initAnnotationTool];
}

- (void)initAnnotationTool {
    _annotationTool = [[PanoAnnotationTool alloc] initWithView:self.view toolOption: PanoAnnotationToolAnnotation];
    _annotationTool.delegate = self;
}

- (void)resetToolbars {
    self.audioButton.enabled = NO;
    self.videoButton.enabled = NO;
}

- (void)switchToolbarsHidden:(UIGestureRecognizer *)sender {
    if ([_annotationTool isVisible]) {
        return;
    }
    [self hideToolbars:!self.bottomToolbarView.hidden];
}

- (void)hideToolbars:(BOOL)hidden {
    self.topToolbarView.hidden = hidden;
    self.bottomToolbarView.hidden = hidden;
    if (!_wbVideoView) {
        [_poolView updateMediaLayout:@{PanoTopToolBarState: @(hidden)}];
    }
    if (hidden == NO) {
         self.hideToolbarsTimer = [NSTimer scheduledTimerWithTimeInterval:kHienToolbarsTimeInterval target:self selector:@selector(hideToolbarsTimerFire:) userInfo:nil repeats:NO];
    } else {
        if (self.hideToolbarsTimer.isValid) {
            [self.hideToolbarsTimer invalidate];
            self.hideToolbarsTimer = nil;
        }
    }
}

- (void)hideToolbarsTimerFire:(id)userinfo {
    self.topToolbarView.hidden = YES;
    self.bottomToolbarView.hidden = YES;
    if (!_wbVideoView) {
        [_poolView updateMediaLayout:@{PanoTopToolBarState: @1}];
    }
}

- (void)initWhiteboard {
    self.whiteboardViewController = nil;
    self.whiteboardOpened = NO;
}

- (void)openWhiteboard {
    self.whiteboardOpened = YES;
    UIImage * image = [UIImage imageNamed:@"btn.share.normal"];
    [self.whiteboardButton setImage:image forState:UIControlStateNormal];
    if (self.viewAppeared) {
        [self showWhiteboardPage];
    } else {
        self.asyncAlert = kAsyncAlertOpenWhiteboard;
    }
}

- (void)showWhiteboardPage {
    [self dismissPresentedController];
    self.whiteboardViewController = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"WhiteboardView"];
    self.whiteboardViewController.whiteboardViewDelegate = self;
    [self presentViewController:self.whiteboardViewController animated:true completion:nil];
}

- (void)updateWhiteboard {
    if (!_whiteboardOpened) {
        UIImage * image = [UIImage imageNamed:@"btn.share.new"];
        [self.whiteboardButton setImage:image forState:UIControlStateNormal];
    }
}

- (void)closeWhiteboard {
    self.whiteboardOpened = NO;
    if (self.whiteboardViewController) {
        [self.whiteboardViewController dismissViewControllerAnimated:YES completion:nil];
        self.whiteboardViewController = nil;
    }
}

- (void)updateWhiteboardRole:(PanoWBRoleType)newRole {
    _config.wbRole = newRole;
}

- (void)showCountDown:(UInt32)remain {
    if (self.countDownTimer.isValid) {
        [self.countDownTimer invalidate];
        self.countDownTimer = nil;
    }
    self.remainTime = remain;
    self.countDown.text = [self coutDownString:self.remainTime];
    self.countDown.hidden = NO;
    self.countDownTimer = [NSTimer scheduledTimerWithTimeInterval:kCountDownTimeInterval target:self selector:@selector(countDownTimerFire:) userInfo:nil repeats:YES];
}

- (void)hideCountDown {
    self.remainTime = 0;
    self.countDown.hidden = YES;
    if (self.countDownTimer.isValid) {
        [self.countDownTimer invalidate];
        self.countDownTimer = nil;
    }
}

- (void)countDownTimerFire:(id)userinfo {
    if (self.remainTime > 0) {
        self.remainTime--;
    }
    self.countDown.text = [self coutDownString:self.remainTime];
}

- (NSString *)coutDownString:(UInt32)remain {
    return [NSString stringWithFormat:@"%02u:%02u:%02u", (unsigned)remain/3600, (unsigned)remain%3600/60, (unsigned)remain%60];
}

- (void)initAsyncAlert {
    self.viewAppeared = NO;
    self.asyncAlert = kAsyncAlertNone;
}

- (void)checkAsyncAlert {
    if (self.viewAppeared) {
        switch (self.asyncAlert) {
            case kAsyncAlertFetchFail:
                [self presentAlert:NSLocalizedString(@"fetchFail", nil)];
                break;
            case kAsyncAlertJoinFail:
                [self presentAlert:NSLocalizedString(@"joinFail", nil)];
                break;
            case kAsyncAlertExit:
                [self presentExitAlert];
                break;
            case kAsyncAlertOpenWhiteboard:
                if (self.whiteboardOpened) {
                    [self showWhiteboardPage];
                }
                break;
            default:
                break;
        }
        self.asyncAlert = kAsyncAlertNone;
    }
}

- (void)joinCall {
    self.callLeft = NO;
    UIApplication.sharedApplication.idleTimerDisabled = YES;
    
    PanoCallClient.shared.channelDelegate = self;
    
    PanoRtcChannelConfig * channelConfig = [[PanoRtcChannelConfig alloc] init];
    channelConfig.mode = kPanoChannelMeeting;
    channelConfig.subscribeAudioAll = YES;
    channelConfig.userName = PanoCallClient.shared.userName;
    PanoResult result = [PanoCallClient.shared.engineKit
                         joinChannelWithToken:kDemoTempToken
                                    channelId:PanoCallClient.shared.roomId
                                       userId:PanoCallClient.shared.userId
                                       config:channelConfig];
    if (kPanoResultOK != result) {
        [self displayMessage:[NSString stringWithFormat:@"Join channel fail with %ld",(long)result]];
        [self.joinIndicator stopAnimating];
        if (self.viewAppeared) {
            [self presentAlert:NSLocalizedString(@"joinFail", nil)];
        } else {
            self.asyncAlert = kAsyncAlertJoinFail;
        }
    } else {
        [self->_userService onUserJoinIndication:PanoCallClient.shared.userId withName:PanoCallClient.shared.userName];
    }
}

- (void)leaveCall {
    self.callLeft = YES;
    // The audio dump need stop by flag.
    if (PanoCallClient.shared.debugMode) {
        [PanoCallClient.shared stopAudioDump];
    }
    [self hideCountDown];
    [_poolView stopRender];
    [self removeObservers];
    [self dismissToRootViewController];
    UIApplication.sharedApplication.idleTimerDisabled = NO;
    [PanoCallClient.shared stop];
    [PanoCallClient.shared start];
}

- (void)enableSpeaker:(BOOL)enable {
    self.speakerStatus = enable;
    UIImage * image = [UIImage imageNamed:(enable ? @"btn.speaker.open" : @"btn.speaker.close")];
    [self.speakerButton setImage:image forState:UIControlStateNormal];
    [PanoCallClient.shared.engineKit setLoudspeakerStatus:enable];
}

- (void)enableAudio:(BOOL)enable {
    [PanoCallClient.shared.audio switchAudioEnable:enable];
}

- (void)updateAudioButtonStatus:(BOOL)activing {
    BOOL mute = _userService.me.isAudioMuted;
    NSString *imageName = (mute ? @"btn.audio.mute" : @"btn.audio.unmute");
    UIImage * image = [UIImage imageNamed:imageName];
    if (!mute && activing) {
        NSInteger i = 0;
        NSMutableArray *images = [NSMutableArray array];
        while (i < 10) {
           NSString *name = [NSString stringWithFormat:@"btn.audio.unmute.active%ld", (long)i];
           [images addObject:[UIImage imageNamed:name]];
           i++;
        }
        image =[UIImage animatedImageWithImages:images duration:1];
    }
    [self.audioButton setImage:image forState:UIControlStateNormal];
    NSString * title = NSLocalizedString(mute ? @"unmute" : @"mute", nil);
    [self.audioButton setTitle:title forState:UIControlStateNormal];
    [self setButtonImageAndTitleEdgeInsets:self.audioButton];
}

- (void)muteAudio:(BOOL)mute {
    !mute && (_shownMuteTip = false);
    [PanoCallClient.shared.audio muteAudio:mute];
    [self updateAudioButtonStatus:false];
}

- (void)enableVideo:(BOOL)enable {
    UIImage * image = [UIImage imageNamed:(enable ? @"btn.video.open" : @"btn.video.close")];
    [self.videoButton setImage:image forState:UIControlStateNormal];
    NSString * title = NSLocalizedString(enable ? @"closeVideo" : @"openVideo", nil);
    [self.videoButton setTitle:title forState:UIControlStateNormal];
    [self setButtonImageAndTitleEdgeInsets:self.videoButton];
    [PanoCallClient.shared.video switchVideoEnable:enable];
    if (enable) {
        [self showTipOfAdjustVideoResolution];
    }
    self.switchCameraBtn.hidden = !enable;
}

- (void)showTipOfAdjustVideoResolution {
    PanoVideoProfileType profile = PanoCallClient.shared.videoProfile;
    if (profile >= PanoCallClient.shared.resolution &&
        profile != kPanoProfileNone) {
        return;
    }
    NSString *msg = @"";
    NSString *otherMsg = nil;
    switch (PanoCallClient.shared.videoProfile) {
        case kPanoProfileNone:
            msg = NSLocalizedString(@"您的手机性能不足，关闭了您的视频", nil);
            break;
        case kPanoProfileLowest:
            otherMsg = @"90p";
            break;
        case kPanoProfileLow:
            otherMsg = @"180p";
            break;
        case kPanoProfileStandard:
            otherMsg = @"360p";
            break;
        case kPanoProfileHD720P:
            otherMsg = @"720p";
            break;
        default:
            break;
    }
    msg = otherMsg ? [NSString stringWithFormat:NSLocalizedString(@"您的手机性能不足，调整你的视频分辨率为%@", nil), otherMsg] : msg;
    [self showToast:msg];
}

- (void)checkAutoStart {
    // Enable audio and video button.
    self.audioButton.enabled = YES;
    self.videoButton.enabled = YES;
    
    // The audio start automatically and mute by flag.
    [self enableAudio:YES];
    if (PanoCallClient.shared.autoMute) {
        [self muteAudio:YES];
    }
    
    // The audio dump start by flag.
    if (PanoCallClient.shared.debugMode) {
        [PanoCallClient.shared startAudioDump];
    }
    
    // The video start by flag.
    if (PanoCallClient.shared.autoVideo) {
        [self enableVideo:YES];
    }
}

- (void)setButtonImageAndTitleEdgeInsets:(UIButton *)button {
    static CGFloat space = 10.0;
    CGSize imageSize = button.imageView.frame.size;
    CGSize titleSize = button.titleLabel.frame.size;
    [button setImageEdgeInsets:UIEdgeInsetsMake(-titleSize.height - space/2.0, 0, 0, -titleSize.width)];
    [button setTitleEdgeInsets:UIEdgeInsetsMake(0, -imageSize.width, -imageSize.height - space/2.0, 0)];
}

- (void)setViewRoundedCorners:(UIView *)view
                   withRadius:(CGFloat)radius {
    view.layer.cornerRadius = radius;
    view.layer.masksToBounds = YES;
}

- (void)addObservers {
    [PanoCallClient.shared addObserver:self forKeyPath:kResolutionObserverKey options:NSKeyValueObservingOptionNew|NSKeyValueObservingOptionOld context:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(statusBarOrientationDidChange) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
}

- (void)removeObservers {
    [PanoCallClient.shared removeObserver:self forKeyPath:kResolutionObserverKey];
    [PanoCallClient.shared.wb removeDelegate:self];
}

- (void)onMainUserStatisticsChanged:(NSDictionary *)info {
    PanoViewInstanceType type = _poolView.medias.firstObject.instance.type;
    if (type == PanoViewInstance_Desktop && info[@"screen"]) {
        _infoLabel.text = info[@"screen"];
    } else if (type == PanoViewInstance_Video && info[@"video"]) {
        _infoLabel.text = info[@"video"];
    }
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(updateMainUserInfo) object:nil];
    [self performSelector:@selector(updateMainUserInfo) withObject:nil afterDelay:2];
}

- (void)updateMainUserInfo {
    PanoUserInfo *user = [self.userService findUserWithId:self.poolView.mainUserId];
    if (user.screenStatus != PanoUserScreen_Unmute && user.videoStaus != PanoUserVideo_Unmute) {
        self.infoLabel.text = NSLocalizedString(@"解码器类型: -\n分辨率@帧率: -\n码率: -", nil);
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary<NSString *,id> *)change
                       context:(void *)context
{
    if ([keyPath isEqualToString:kResolutionObserverKey]) {
        NSNumber * newValue = [change objectForKey:@"new"];
        NSNumber * oldValue = [change objectForKey:@"old"];
        if (newValue.integerValue != oldValue.integerValue) {
            if (!_userService.me.isVideoMuted) {
                [self.poolView updateVideoRenderConfig:nil];
                [self showTipOfAdjustVideoResolution];
            }
        }
    }
}

- (void)statusBarOrientationDidChange {
    if (_wbVideoView) {
        _wbVideoView.frame = CGRectMake(self.view.bounds.size.width-117.5-15, 88, 117.5, 157.5);
    }
    [self updateContentViewSize];
    [_annotationTool.penView dismiss];
}

- (void)updateContentViewSize {
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    PanoViewPageLayoutType type = _poolView.layoutType;
    if (type == PanoViewPageLayout_4_Avg && !UIInterfaceOrientationIsLandscape(orientation)) {
        [self.poolView.contentView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.left.right.mas_equalTo(self.view);
            make.top.mas_equalTo(self.topToolbarView.mas_bottom).mas_offset(15);
            make.bottom.mas_equalTo(self.bottomToolbarView.mas_top).mas_offset(-15);
        }];
        
    } else {
        if (type == PanoViewPageLayout_4_Avg) {
            [self hideToolbars:true];
        }
        [self.poolView.contentView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.mas_equalTo(self.view);
        }];
    }
}

#pragma mark -- PanoPoolViewDelegate
- (void)onPageTypeChanged:(PanoViewPageLayoutType)type {
    [self updateContentViewSize];
}

- (void)onPageIndexChanged:(NSUInteger)index numbersOfIndexs:(NSUInteger)totalIndexs {
    self.layoutControl.hidden = totalIndexs <= 1;
    self.layoutControl.currentPage = index;
    self.layoutControl.numberOfPages = totalIndexs;
    self.infoLabel.hidden = index > 0 || !PanoCallClient.shared.staticsFlag;
    self.infoLabel.superview.hidden = self.infoLabel.hidden;
}

- (void)onAudioActiveUserChanged:(PanoViewInstance *)instance activing:(BOOL)activing {
    if (!_wbVideoView) {
        return;
    }
    PanoPoolService *poolService = PanoCallClient.shared.pool;
    instance = poolService.activeSpeakerUser;
    instance.mode = PanoViewInstance_Float;
    activing = true;
    if (instance) {
        if (instance.userId == _wbVideoView.instance.userId) {
            return;
        }
        if (!activing) {
            return;
        }
        [_wbVideoView stop];
        instance.mode = PanoViewInstance_Float;
        _wbVideoView.instance = instance;
        _wbVideoView.hidden = false;
        [_wbVideoView start];
    } else {
        [_wbVideoView stop];
        _wbVideoView.hidden = true;
        _wbVideoView.instance = nil;
    }
}

- (void)onMyAudioActiveChanged:(BOOL)activing {
    [self updateAudioButtonStatus:activing];
}

- (void)onAudioActiveUserStatusChanged:(PanoViewInstance *)instance {
    if (!_wbVideoView) {
        return;
    }
    [_wbVideoView stop];
    _wbVideoView.instance = instance;
    [_wbVideoView start];
}

- (void)onSpeakingWhenTheAudioMuted {
    if (_userService.me.audioStatus != PanoUserAudio_Mute) {
        return;
    }
    if (_shownMuteTip) {
        return;
    }
    _shownMuteTip = true;
    [self showToast:NSLocalizedString(@"您已静音，请开启麦克风后发言", nil)];
}

- (IBAction)openUserListPage:(id)sender {
    PanoUserListViewController *list = [[PanoUserListViewController alloc] init];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:list];
    navi.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:navi animated:true completion:^{
    }];
}

- (NSString *)videoInfoDescription:(PanoRtcScreenRecvStats *)stats {
    PanoVideoCodecType codec = stats.codecType;
    BOOL isEncodec = [stats isKindOfClass:[PanoRtcVideoSendStats class]];
    NSString *frame = [NSString stringWithFormat:@"%@%d x %d@%d", NSLocalizedString(@"分辨率@帧率: ", nil), (int)stats.width, (int)stats.height, stats.framerate];
    NSString *code = isEncodec ? NSLocalizedString(@"编码器类型: ", nil) : NSLocalizedString(@"解码器类型: ", nil);
    NSString *videoCodec = [NSString stringWithFormat:@"%@%@", code, codec == 0 ? NSLocalizedString(@"未知", nil) : (codec == 1 ? @"H.264" : @"AV1X")];
    NSString *bitrate = @"-";
    if ([stats isKindOfClass:[PanoRtcScreenRecvStats class]]) {
        bitrate = [NSString stringWithFormat:@"%@ %lldkbps", NSLocalizedString(@"码率: ", nil), stats.recvBitrate/1000];
    } else {
        bitrate = [NSString stringWithFormat:@"%@ %lldkbps", NSLocalizedString(@"码率: ", nil), ((PanoRtcScreenSendStats *)stats).sendBitrate/1000];
    }
    return [NSString stringWithFormat:@"%@\n%@\n%@",videoCodec,frame,bitrate ];
}

- (void)dealloc {
    NSLog(@"dealloc: %@", self);
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
@end
