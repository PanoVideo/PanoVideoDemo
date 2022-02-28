//
//  CallViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "CallViewController.h"
#import "UIViewController+Extension.h"
#import "UIAlertController+Blocks.h"
#import "PanoUserListViewController.h"
#import "PanoCallClient.h"
#import "PanoPoolView.h"
#import "PanoVideoView.h"
#import "Reachability.h"
#import "MBProgressHUD+Extension.h"
#import "PanoPermission.h"

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

@interface CallViewController ()
<PanoRtcEngineDelegate,
PanoRtcWhiteboardDelegate,
PanoPoolViewDelegate,
PanoWhiteboardDelegate>

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
@property (strong, nonatomic) NSTimer * countDownTimer;
@property (assign, nonatomic) UInt32 remainTime;
@property (strong, nonatomic) NSTimer * hideToolbarsTimer;
@property (assign, nonatomic) BOOL callLeft;
@property (assign, nonatomic) BOOL viewAppeared;
@property (assign, nonatomic) AsyncAlertType asyncAlert;
@property (assign, nonatomic) UInt64 backMainUserId;
@property (strong, nonatomic) Reachability *reachability;
@property (assign, nonatomic) BOOL shownMuteTip;
@property (weak, nonatomic) IBOutlet UILabel *infoLabel;
@property (weak, nonatomic) PanoUserService *userService;
@property (strong, nonatomic) MBProgressHUD *reconnectingHUD;
@property (strong, nonatomic) UITapGestureRecognizer *tapGesture;
@end

@implementation CallViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _userService = PanoCallClient.shared.userMgr;
    [self setLocalizable];
    [self initToolbars];
    [self initLayout];
    [self initGestureRecognizers];
    [self hideCountDown];
    [self initAsyncAlert];
    [self addObservers];
    [self joinCall];
    [UIApplication.sharedApplication setIdleTimerDisabled:true];
    _reachability = [Reachability reachabilityForInternetConnection];
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
    if (![PanoPermission checkAudioPermission]) {
        [self showPermissionAlertWithType:true];
        return;
    }
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
    UIAlertAction *wbAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"白板标注", nil) style:UIAlertActionStyleDefault
        handler:^(UIAlertAction * _Nonnull action) {
        if ([PanoCallClient.shared.wb isOn]) {
            [self enableAnnotation:true];
            [self.poolView enableWhiteboard:true];
        }
    }];
    if (![_userService isHost] && [PanoCallClient.shared.wb isOn]) {
        [alert addAction:wbAction];
    }
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
        if (PanoCallClient.shared.userMgr.sharingUser != nil) {
            NSString *msg = [NSString stringWithFormat:@"这将中断'%@'的共享，是否继续",  PanoCallClient.shared.userMgr.sharingUser.userName];
            [UIAlertController showAlertInViewController:self withTitle:NSLocalizedString(@"打开白板", nil) message:msg cancelButtonTitle:NSLocalizedString(@"取消", nil) destructiveButtonTitle:nil otherButtonTitles:@[NSLocalizedString(@"确定", nil)] tapBlock:^(UIAlertController * _Nonnull controller, UIAlertAction * _Nonnull action, NSInteger buttonIndex) {
                if (buttonIndex == controller.firstOtherButtonIndex) {
                    [PanoCallClient.shared.screen stopShare];
                    [PanoCallClient.shared.wb open];
                }
            }];
        } else {
            [PanoCallClient.shared.wb open];
        }
    }];
    if ([_userService isHost]) {
        [alert addAction:annotation];
    }
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        alert.popoverPresentationController.sourceView = sender;
        alert.popoverPresentationController.sourceRect = sender.bounds;
    }
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)dismissPresentedController {
    NSLog(@"self.presentedViewController: %@",self.presentedViewController);
    UIViewController *vc = self.presentedViewController;
    if (vc && ([vc isKindOfClass:[UIAlertController class]])) {
        [vc dismissViewControllerAnimated:true completion:nil];
    }
}

- (void)openSettingPage {
    UIViewController *settingVc = [[UIStoryboard storyboardWithName:@"Main" bundle:nil] instantiateViewControllerWithIdentifier:@"setting"];
    [self presentViewController:settingVc animated:true completion:nil];
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
    [self showCountDown:remain];
}

- (void)onNetworkQuality:(PanoQualityRating)quality withUser:(UInt64)userId {
    for (PanoBaseMediaView *mediaView in self.poolView.medias) {
        if (mediaView.instance.userId == userId) {
            [mediaView update:@{PanoNetworkStatus : @(quality)}];
            break;
        }
    }
}

#pragma mark - PanoRtcWhiteboardDelegate
- (void)onRoleTypeChanged:(PanoWBRoleType)newRole {
    [PanoCallClient shared].config.wbRole = newRole;
}

#pragma mark - WhiteboardViewDelegate
- (void)onVisionShareStarted:(UInt64)userId {
    PanoRtcWhiteboard *wb = PanoCallClient.shared.engineKit.whiteboardEngine;
    PanoUserInfo *info = [_userService findUserWithId:userId];
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"'%@'开启了视角跟随", nil), info.userName] ;
    [self showMessage:msg];
    [wb startFollowVision];
}

- (void)onVisionShareStopped:(UInt64)userId {
    PanoUserInfo *info = [_userService findUserWithId:userId];
    NSString *msg = [NSString stringWithFormat:NSLocalizedString(@"'%@'关闭了视觉跟随", nil), info.userName] ;
    [self showMessage:msg];
    PanoRtcWhiteboard *wb = PanoCallClient.shared.engineKit.whiteboardEngine;
    [wb stopFollowVision];
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
    _tapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchToolbarsHidden:)];
    _tapGesture.numberOfTapsRequired = 1;
    _tapGesture.numberOfTouchesRequired  = 1;
    [self.view addGestureRecognizer:_tapGesture];
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
}

- (void)resetToolbars {
    self.audioButton.enabled = NO;
    self.videoButton.enabled = NO;
}

- (void)switchToolbarsHidden:(UIGestureRecognizer *)sender {
    [self hideToolbars:!self.bottomToolbarView.hidden];
}

- (void)hideToolbars:(BOOL)hidden {
    self.topToolbarView.hidden = hidden;
    self.bottomToolbarView.hidden = hidden;
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
}

- (void)enableGestures:(BOOL)enable {
    [self.view.gestureRecognizers enumerateObjectsUsingBlock:^(__kindof UIGestureRecognizer * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.enabled = enable;
    }];
}

- (void)enableAnnotation:(BOOL)enable {
    [self enableGestures:!enable];
    [self hideToolbars:enable];
}

#pragma mark -- PanoWhiteboardDelegate
- (void)onWhiteboardStatusChanged:(BOOL)on {
    [self enableAnnotation:on];
}
- (void)onPresenterDidChanged {
    if ([_userService isHost]) {
        if (PanoCallClient.shared.wb.isOn) {
            [self enableAnnotation:true];
        }
    }
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
    [self hideCountDown];
    [_poolView stopRender];
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

- (void)showPermissionAlertWithType:(BOOL)isAudio {
    NSString *title = NSLocalizedString(isAudio ? @"无法使用麦克风" : @"无法使用相机", nil);
    NSString *msg = NSLocalizedString(isAudio ? @"请在iPhone的‘设置-隐私-麦克风’中允许PanoVideoCall访问您的麦克风" : @"请在iPhone的‘设置-隐私-相机’中允许PanoVideoCall访问您的麦克风", nil);
    [UIAlertController showAlertInViewController:self withTitle:title message:msg cancelButtonTitle:NSLocalizedString(@"取消", nil) destructiveButtonTitle:NSLocalizedString(@"前往设置", nil) otherButtonTitles:nil tapBlock:^(UIAlertController * _Nonnull controller, UIAlertAction * _Nonnull action, NSInteger buttonIndex) {
        if (buttonIndex == controller.destructiveButtonIndex) {
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        }
    }];
}

- (void)updateAudioButtonStatus:(BOOL)activing {
    BOOL mute = _userService.me.isAudioMuted;
    NSString *imageName = (mute ? @"btn.audio.mute" : @"btn.audio.unmute");
    UIImage * image = [UIImage imageNamed:imageName];
    if (!mute && activing) {
        image = [_userService.me activeAudioImage];
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
    if (enable && ![PanoPermission checkVideoPermission]) {
        [self showPermissionAlertWithType:false];
        return;
    }
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
    [self showMessage:msg];
}

- (void)checkAutoStart {
    self.audioButton.enabled = YES;
    self.videoButton.enabled = YES;
    
    if (![PanoPermission checkAudioPermission]) {
        [self showPermissionAlertWithType:true];
        [self muteAudio:YES];
    } else {
        [self enableAudio:YES];
        if (PanoCallClient.shared.autoMute) {
            [self muteAudio:YES];
        }
    }
    if (PanoCallClient.shared.debugMode) {
        [PanoCallClient.shared startAudioDump];
    }
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
    [PanoCallClient.shared.wb addDelegate:self];
//    [PanoCallClient.shared.userMgr addDelegate:self];
}

- (void)removeObservers {
    [PanoCallClient.shared removeObserver:self forKeyPath:kResolutionObserverKey];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [PanoCallClient.shared.wb removeDelegate:self];
//    [PanoCallClient.shared.userMgr removeDelegate:self];
}

#pragma mark -- 主视图显示视频，共享统计信息
- (void)onVideoSendStats:(PanoRtcVideoSendStats * _Nonnull)stats {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (PanoCallClient.shared.userId == self.poolView.mainUserId) {
            [self onMainUserStatisticsChanged:@{@"video": [self videoInfoDescription:(PanoRtcScreenRecvStats *)stats]}];
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

- (void)onScreenRecvStats:(PanoRtcScreenRecvStats * _Nonnull)stats {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (stats.userId == self.poolView.mainUserId) {
            [self onMainUserStatisticsChanged:@{@"screen": [self videoInfoDescription:stats]}];
        }
    });
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
    if (user.screenStatus != PanoUserScreen_Unmute &&
        user.videoStaus != PanoUserVideo_Unmute ) {
        self.infoLabel.text = NSLocalizedString(@"解码器类型: -\n分辨率@帧率: -\n码率: -", nil);
    }
}

#pragma mark -- KVO
- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary<NSString *,id> *)change
                       context:(void *)context {
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
    [self updateContentViewSize];
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
    self.layoutControl.hidden = !_tapGesture.enabled ? true : totalIndexs <= 1;
    self.layoutControl.currentPage = index;
    self.layoutControl.numberOfPages = totalIndexs;
    self.infoLabel.hidden = index > 0 || !PanoCallClient.shared.staticsFlag;
    self.infoLabel.superview.hidden = self.infoLabel.hidden;
}

- (void)onMyAudioActiveChanged:(BOOL)activing {
    [self updateAudioButtonStatus:activing];
}

- (void)onSpeakingWhenTheAudioMuted {
    if (_userService.me.audioStatus != PanoUserAudio_Mute) {
        return;
    }
    if (_shownMuteTip) {
        return;
    }
    _shownMuteTip = true;
    [self showMessage:NSLocalizedString(@"您已静音，请开启麦克风后发言", nil)];
}

- (IBAction)openUserListPage:(id)sender {
    PanoUserListViewController *list = [[PanoUserListViewController alloc] init];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:list];
    navi.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:navi animated:true completion:NULL];
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
    [self removeObservers];
}
@end
