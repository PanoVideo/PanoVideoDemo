//
//  CallViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "CallViewController.h"
#import "WhiteboardViewController.h"
#import "PanoCallClient.h"
#import "UserInfo.h"

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

@interface CallViewController () <PanoRtcEngineDelegate, PanoRtcWhiteboardDelegate, WhiteboardViewDelegate>

@property (strong, nonatomic) IBOutlet UIView * topToolbarView;
@property (strong, nonatomic) IBOutlet UIButton * speakerButton;
@property (strong, nonatomic) IBOutlet UILabel * roomId;
@property (strong, nonatomic) IBOutlet UILabel * countDown;

@property (strong, nonatomic) IBOutlet UIView * bottomToolbarView;
@property (strong, nonatomic) IBOutlet UIButton * audioButton;
@property (strong, nonatomic) IBOutlet UIButton * videoButton;
@property (strong, nonatomic) IBOutlet UIButton * whiteboardButton;
@property (strong, nonatomic) IBOutlet UIButton * settingButton;

@property (strong, nonatomic) IBOutlet UIView * floatView;
@property (strong, nonatomic) IBOutlet UserView * userTopView;
@property (strong, nonatomic) IBOutlet UserView * userLeftView;
@property (strong, nonatomic) IBOutlet UserView * userRightView;
@property (strong, nonatomic) IBOutlet UserView * userMainView;

@property (strong, nonatomic) IBOutlet UIView * wallView;
@property (strong, nonatomic) IBOutlet UserView * user1View;
@property (strong, nonatomic) IBOutlet UserView * user2View;
@property (strong, nonatomic) IBOutlet UserView * user3View;
@property (strong, nonatomic) IBOutlet UserView * user4View;

@property (strong, nonatomic) IBOutlet UIPageControl * layoutControl;

@property (strong, nonatomic) IBOutlet UIActivityIndicatorView * joinIndicator;

@property (strong, nonatomic) UserInfo * localUserInfo;
@property (strong, nonatomic) UserManager * remoteUserManager;
@property (assign, nonatomic) BOOL speakerStatus;

@property (strong, nonatomic) UISwipeGestureRecognizer *leftSwipeGesture;
@property (strong, nonatomic) UISwipeGestureRecognizer *rightSwipeGesture;

@property (weak, nonatomic) WhiteboardViewController * whiteboardViewController;
@property (assign, nonatomic) BOOL whiteboardOpened;

@property (strong, nonatomic) NSTimer * countDownTimer;
@property (assign, nonatomic) UInt32 remainTime;

@property (strong, nonatomic) NSTimer * hideToolbarsTimer;

@property (assign, nonatomic) BOOL callLeft;

@property (assign, nonatomic) BOOL viewAppeared;
@property (assign, nonatomic) AsyncAlertType asyncAlert;

@property (assign, nonatomic) UInt64 backMainUserId;

@end

@implementation CallViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setLocalizable];
    [self initToolbars];
    [self initLayout];
    [self initGestureRecognizers];
    [self initUserInfos];
    [self initWhiteboard];
    [self hideCountDown];
    [self initAsyncAlert];
    [self addObservers];
    [self joinCall];
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
    if (PanoCallClient.sharedInstance.leaveConfirm) {
        [self presentExitConfirm];
    } else {
        [self leaveCall];
    }
}

- (IBAction)switchSpeaker:(id)sender {
    [self enableSpeaker:!self.speakerStatus];
}

- (IBAction)switchCamera:(id)sender {
    [PanoCallClient.sharedInstance.engineKit switchCamera];
    if (self.localUserInfo.videoEnable) {
        [self reEnableVideo];
    }
}

- (IBAction)clickAudio:(id)sender {
    [self muteAudio:!self.localUserInfo.audioMute];
}

- (IBAction)clickVideo:(id)sender {
    [self enableVideo:!self.localUserInfo.videoEnable];
}

#pragma mark - Navigation

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(nullable id)sender {
    BOOL perform = NO;
    if ([identifier compare:@"Whiteboard"] == NSOrderedSame) {
        if (PanoCallClient.sharedInstance.host || self.whiteboardOpened) {
            perform = YES;
        } else {
            [self presentAlert:NSLocalizedString(@"whiteboardOff", nil)];
        }
    } else {
        perform = YES;
    }
    return perform;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(nullable id)sender {
    if ([segue.identifier compare:@"Whiteboard"] == NSOrderedSame) {
        [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:nil];
        self.whiteboardViewController = (WhiteboardViewController *)segue.destinationViewController;
        self.whiteboardViewController.whiteboardViewDelegate = self;
        UIImage * image = [UIImage imageNamed:@"btn.whiteboard.open"];
        [self.whiteboardButton setImage:image forState:UIControlStateNormal];
    }
}

#pragma mark - PanoRtcEngineDelegate

- (void)onChannelJoinConfirm:(PanoResult)result {
    [self displayMessage:[NSString stringWithFormat:@"Join channel confirm with %ld", (long)result]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.joinIndicator stopAnimating];
        if (kPanoResultOK == result) {
            [self checkAutoStart];
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
        [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:nil];
        [self closeWhiteboard];
        [self enableWhiteboard:NO];
        [self hideCountDown];
        [self resetLayout];
        [self resetToolbars];
        if (self.viewAppeared) {
            [self presentExitAlert];
        } else {
            self.asyncAlert = kAsyncAlertExit;
        }
    });
}

- (void)onChannelFailover:(PanoFailoverState)state {
    [self displayMessage:[NSString stringWithFormat:@"Channel failover with %ld", (long)state]];
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
        [self addRemoteUser:userId withName:userName];
    });
}

- (void)onUserLeaveIndication:(UInt64)userId
                   withReason:(PanoUserLeaveReason)reason {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu leave room with %ld", (unsigned long)userId, (long)reason]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self removeRemoteUser:userId];
    });
}

- (void)onUserAudioStart:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu start audio", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withAudioEnable:YES];
    });
}

- (void)onUserAudioStop:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu stop audio", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withAudioEnable:NO];
    });
}

- (void)onUserAudioMute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu mute audio", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withAudioMute:YES];
    });
}

- (void)onUserAudioUnmute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu unmute audio", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withAudioMute:NO];
    });
}

- (void)onUserVideoStart:(UInt64)userId
          withMaxProfile:(PanoVideoProfileType)maxProfile {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu start video with max profile %ld", (unsigned long)userId, (long)maxProfile]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withVideoEnable:YES];
    });
}

- (void)onUserVideoStop:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu stop video", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withVideoEnable:NO];
    });
}

- (void)onUserVideoMute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu mute video", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withVideoMute:YES];
    });
}

- (void)onUserVideoUnmute:(UInt64)userId {
    [self displayMessage:[NSString stringWithFormat:@"The user %lu unmute video", (unsigned long)userId]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateRemoteUserInfo:userId withVideoMute:NO];
    });
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
}

- (void)onVideoRecvStats:(PanoRtcVideoRecvStats * _Nonnull)stats {
}

- (void)onScreenSendStats:(PanoRtcScreenSendStats * _Nonnull)stats {
}

- (void)onScreenRecvStats:(PanoRtcScreenRecvStats * _Nonnull)stats {
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
        [self enableWhiteboard:YES];
        [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:self];
    });
}

- (void)onWhiteboardUnavailable {
    [self displayMessage:[NSString stringWithFormat:@"Whiteboard unavailable"]];
    dispatch_async(dispatch_get_main_queue(), ^{
        [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:nil];
        [self closeWhiteboard];
        [self enableWhiteboard:NO];
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
        [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:self];
    });
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

- (void)whiteboardViewDidOpen {
    [self switchToWhiteboard];
}

- (void)whiteboardViewWillClose {
    [self switchFromWhiteboard];
}

- (void)whiteboardViewDidClose {
    [PanoCallClient.sharedInstance.engineKit.whiteboardEngine setDelegate:self];
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
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        [self leaveCall];
    }];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"cancel", nil)
                                                      style:UIAlertActionStyleDefault
                                                    handler:nil];
    [alert addAction:cancel];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - Private

- (void)displayMessage:(NSString *)message {
#if defined(DEBUG)
    NSLog(@"%@", message);
#endif
}

- (void)setLocalizable {
    self.roomId.text = PanoCallClient.sharedInstance.roomId;
}

- (void)initLayout {
    [self resetLayout];
    [self setViewRoundedCorners:self.userTopView withRadius:5.0];
    [self setViewRoundedCorners:self.userLeftView withRadius:5.0];
    [self setViewRoundedCorners:self.userRightView withRadius:5.0];
}

- (void)resetLayout {
    self.userMainView.hidden = YES;
    self.userTopView.hidden = YES;
    self.userLeftView.hidden = YES;
    self.userRightView.hidden = YES;
    self.user1View.hidden = YES;
    self.user2View.hidden = YES;
    self.user3View.hidden = YES;
    self.user4View.hidden = YES;
    
    self.layoutControl.currentPage = 0;
    self.floatView.hidden = NO;
    self.wallView.hidden = YES;
    [self enableSwitchLayout:NO];
}

- (void)initGestureRecognizers {
    // handle swipe gesture recognizers for switch layout
    self.leftSwipeGesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(switchLayout:)];
    self.leftSwipeGesture.direction = UISwipeGestureRecognizerDirectionLeft;
    self.rightSwipeGesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(switchLayout:)];
    self.rightSwipeGesture.direction = UISwipeGestureRecognizerDirectionRight;
    
    // handle single tap gesture recognizers for toolbar hidden
    UITapGestureRecognizer *singleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchToolbarsHidden:)];
    singleTapGesture.numberOfTapsRequired = 1;
    singleTapGesture.numberOfTouchesRequired  = 1;
    [self.view addGestureRecognizer:singleTapGesture];
    
    // handle double tap gesture recognizer for switch user
    UITapGestureRecognizer *doubleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchTopUser:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    doubleTapGesture.numberOfTouchesRequired  = 1;
    [self.userTopView addGestureRecognizer:doubleTapGesture];
    [singleTapGesture requireGestureRecognizerToFail:doubleTapGesture];
    
    doubleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchLeftUser:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    doubleTapGesture.numberOfTouchesRequired  = 1;
    [self.userLeftView addGestureRecognizer:doubleTapGesture];
    [singleTapGesture requireGestureRecognizerToFail:doubleTapGesture];
    
    doubleTapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(switchRightUser:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    doubleTapGesture.numberOfTouchesRequired  = 1;
    [self.userRightView addGestureRecognizer:doubleTapGesture];
    [singleTapGesture requireGestureRecognizerToFail:doubleTapGesture];
}

- (void)enableSwitchLayout:(BOOL)enable {
    self.layoutControl.hidden = !enable;
    if (enable) {
        [self.view addGestureRecognizer:self.leftSwipeGesture];
        [self.view addGestureRecognizer:self.rightSwipeGesture];
    } else {
        [self.view removeGestureRecognizer:self.leftSwipeGesture];
        [self.view removeGestureRecognizer:self.rightSwipeGesture];
    }
}

- (void)initUserInfos {
    self.localUserInfo = [[UserInfo alloc] initWithId:PanoCallClient.sharedInstance.userId
                                                 name:PanoCallClient.sharedInstance.userName];
    self.localUserInfo.userView = self.userMainView;
    self.remoteUserManager = [UserManager new];
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
    
    [self enableSpeaker:PanoCallClient.sharedInstance.autoSpeaker];
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

- (void)initWhiteboard {
    [self enableWhiteboard:NO];
    self.whiteboardViewController = nil;
    self.whiteboardOpened = NO;
}

- (void)enableWhiteboard:(BOOL)enable {
    self.whiteboardButton.enabled = enable;
    BOOL canOpen = PanoCallClient.sharedInstance.host && enable;
    UIImage * image = [UIImage imageNamed:canOpen ? @"btn.whiteboard.open" : @"btn.whiteboard.close"];
    [self.whiteboardButton setImage:image forState:UIControlStateNormal];
}

- (void)openWhiteboard {
    self.whiteboardOpened = YES;
    UIImage * image = [UIImage imageNamed:@"btn.whiteboard.open"];
    [self.whiteboardButton setImage:image forState:UIControlStateNormal];
    if (self.viewAppeared) {
        [self performSegueWithIdentifier:@"Whiteboard" sender:nil];
    } else {
        self.asyncAlert = kAsyncAlertOpenWhiteboard;
    }
}

- (void)updateWhiteboard {
    if (self.whiteboardViewController == nil) {
        UIImage * image = [UIImage imageNamed:@"btn.whiteboard.new"];
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
    PanoCallClient.sharedInstance.wbRole = newRole;
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
        self.countDown.text = [self coutDownString:self.remainTime];
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
                if (self.whiteboardOpened) {
                    [self performSegueWithIdentifier:@"Whiteboard" sender:nil];
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
    
    PanoCallClient.sharedInstance.channelDelegate = self;
    [PanoCallClient.sharedInstance recordUsage];
    // fetch token and join channel
    [self.joinIndicator startAnimating];
    PanoRtcChannelConfig * channelConfig = [[PanoRtcChannelConfig alloc] init];
    channelConfig.mode = kPanoChannelMeeting;
    channelConfig.subscribeAudioAll = YES;
    channelConfig.userName = PanoCallClient.sharedInstance.userName;
    PanoResult result = [PanoCallClient.sharedInstance.engineKit
                         joinChannelWithToken:kDemoTempToken
                                    channelId:PanoCallClient.sharedInstance.roomId
                                       userId:PanoCallClient.sharedInstance.userId
                                       config:channelConfig];
    if (kPanoResultOK != result) {
        [self displayMessage:[NSString stringWithFormat:@"Join channel fail with %ld",(long)result]];
        [self.joinIndicator stopAnimating];
        if (self.viewAppeared) {
            [self presentAlert:NSLocalizedString(@"joinFail", nil)];
        } else {
            self.asyncAlert = kAsyncAlertJoinFail;
        }
    }
}

- (void)leaveCall {
    self.callLeft = YES;
    
    // The audio dump need stop by flag.
    if (PanoCallClient.sharedInstance.debugMode) {
        [PanoCallClient.sharedInstance stopAudioDump];
    }
    
    [PanoCallClient.sharedInstance.engineKit leaveChannel];
    PanoCallClient.sharedInstance.channelDelegate = nil;
    
    [self removeObservers];
    [self dismissToRootViewController];
    UIApplication.sharedApplication.idleTimerDisabled = NO;
}

- (void)enableSpeaker:(BOOL)enable {
    self.speakerStatus = enable;
    UIImage * image = [UIImage imageNamed:(enable ? @"btn.speaker.open" : @"btn.speaker.close")];
    [self.speakerButton setImage:image forState:UIControlStateNormal];
    [PanoCallClient.sharedInstance.engineKit setLoudspeakerStatus:enable];
}

- (void)enableAudio:(BOOL)enable {
    self.localUserInfo.audioEnable = enable;
    if (enable) {
        [PanoCallClient.sharedInstance.engineKit startAudio];
    } else {
        [PanoCallClient.sharedInstance.engineKit stopAudio];
    }
}

- (void)muteAudio:(BOOL)mute {
    self.localUserInfo.audioMute = mute;
    UIImage * image = [UIImage imageNamed:(mute ? @"btn.audio.mute" : @"btn.audio.unmute")];
    [self.audioButton setImage:image forState:UIControlStateNormal];
    NSString * title = NSLocalizedString(mute ? @"unmute" : @"mute", nil);
    [self.audioButton setTitle:title forState:UIControlStateNormal];
    [self setButtonImageAndTitleEdgeInsets:self.audioButton];
    if (mute) {
        [PanoCallClient.sharedInstance.engineKit muteAudio];
    } else {
        [PanoCallClient.sharedInstance.engineKit unmuteAudio];
    }
}

- (void)enableVideo:(BOOL)enable {
    self.localUserInfo.videoEnable = enable;
    UIImage * image = [UIImage imageNamed:(enable ? @"btn.video.open" : @"btn.video.close")];
    [self.videoButton setImage:image forState:UIControlStateNormal];
    NSString * title = NSLocalizedString(enable ? @"closeVideo" : @"openVideo", nil);
    [self.videoButton setTitle:title forState:UIControlStateNormal];
    [self setButtonImageAndTitleEdgeInsets:self.videoButton];
    if (enable) {
        PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
        renderConfig.profileType = PanoCallClient.sharedInstance.resolution;
        renderConfig.scalingMode = kPanoScalingCropFill;
        renderConfig.mirror = [PanoCallClient.sharedInstance.engineKit isFrontCamera];
        [PanoCallClient.sharedInstance.engineKit startVideoWithView:self.localUserInfo.userView config:renderConfig];
    } else {
        [PanoCallClient.sharedInstance.engineKit stopVideo];
    }
}

- (void)reEnableVideo {
    PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
    renderConfig.profileType = PanoCallClient.sharedInstance.resolution;
    renderConfig.scalingMode = kPanoScalingCropFill;
    renderConfig.mirror = [PanoCallClient.sharedInstance.engineKit isFrontCamera];
    [PanoCallClient.sharedInstance.engineKit startVideoWithView:self.localUserInfo.userView config:renderConfig];
}

- (PanoVideoProfileType)profileWithView:(UserView *)view {
    PanoVideoProfileType profile = kPanoProfileStandard;
    CGFloat viewSize = view.bounds.size.width * view.bounds.size.height;
    if ( viewSize <= 160 * 90) {
        profile = kPanoProfileLow;
    } else if (viewSize > 320 * 180) {
        profile = kPanoProfileMax;
    } else {
        profile = kPanoProfileStandard;
    }
    return profile;
}

- (void)subscribeVideo:(UserInfo *)user {
    PanoRtcRenderConfig * renderConfig = [[PanoRtcRenderConfig alloc] init];
    renderConfig.profileType = [self profileWithView:user.userView];
    renderConfig.scalingMode = kPanoScalingCropFill;
    [PanoCallClient.sharedInstance.engineKit subscribeVideo:user.userId withView:user.userView config:renderConfig];
}

- (void)unsubsribeVideo:(UserInfo *)user {
    [PanoCallClient.sharedInstance.engineKit unsubscribeVideo:user.userId];
}

- (void)switchLayout:(UISwipeGestureRecognizer *)sender {
    [self hideToolbars:YES];
    // Current remote user count is not less than 2.
    if (sender.direction == UISwipeGestureRecognizerDirectionLeft) {
        if (self.layoutControl.currentPage == 0) {
            self.layoutControl.currentPage = 1;
            self.floatView.hidden = YES;
            self.wallView.hidden = NO;
            
            self.user1View.hidden = YES;
            self.user2View.hidden = YES;
            self.user3View.hidden = YES;
            self.user4View.hidden = YES;
            
            [self backUserIds];
            
            self.localUserInfo.userView = self.user1View;
            if (self.localUserInfo.videoEnable) {
                [self reEnableVideo];
            }
            UserInfo * remoteUser = [self.remoteUserManager findUserWithIndex:0];
            remoteUser.userView = self.user2View;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
            remoteUser = [self.remoteUserManager findUserWithIndex:1];
            remoteUser.userView = self.user3View;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
            remoteUser = [self.remoteUserManager findUserWithIndex:2];
            if (remoteUser) {
                remoteUser.userView = self.user4View;
                if (remoteUser.videoEnable) {
                    [self subscribeVideo:remoteUser];
                }
            }
        }
    } else if (sender.direction == UISwipeGestureRecognizerDirectionRight) {
        if (self.layoutControl.currentPage == 1) {
            self.layoutControl.currentPage = 0;
            self.floatView.hidden = NO;
            self.wallView.hidden = YES;
            
            self.userMainView.hidden = YES;
            self.userTopView.hidden = YES;
            self.userLeftView.hidden = YES;
            self.userRightView.hidden = YES;
            
            BOOL selfIsMainUser = self.backMainUserId == self.localUserInfo.userId ? YES : NO;
            self.localUserInfo.userView = selfIsMainUser ? self.userMainView : self.userTopView;
            if (self.localUserInfo.videoEnable) {
                [self reEnableVideo];
            }
            
            UserInfo * remoteUser = [self.remoteUserManager findUserWithView:self.user2View];
            remoteUser.userView = selfIsMainUser ? self.userTopView : self.userMainView;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
            remoteUser = [self.remoteUserManager findUserWithView:self.user3View];
            remoteUser.userView = self.userLeftView;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
            remoteUser = [self.remoteUserManager findUserWithView:self.user4View];
            if (remoteUser) {
                remoteUser.userView = self.userRightView;
                if (remoteUser.videoEnable) {
                    [self subscribeVideo:remoteUser];
                }
            }
            if (selfIsMainUser == NO) {
                remoteUser = [self.remoteUserManager findUser:self.backMainUserId];
                if (remoteUser && remoteUser.userView != self.userMainView) {
                    [self switchUserToMainView:remoteUser.userView];
                }
            }
        }
    }
}

- (void)switchTopUser:(UIGestureRecognizer *)sender {
//    NSLog(@"switch top user");
    // Current layout is the float layout.
    [self switchUserToMainView:self.userTopView];
}

- (void)switchLeftUser:(UIGestureRecognizer *)sender {
//    NSLog(@"switch left user");
    // Current layout is the float layout.
    [self switchUserToMainView:self.userLeftView];
}

- (void)switchRightUser:(UIGestureRecognizer *)sender {
//    NSLog(@"switch right user");
    // Current layout is the float layout.
    [self switchUserToMainView:self.userRightView];
}

- (void)switchUserToMainView:(UserView *)userView {
    UserInfo * switchUser = [self.remoteUserManager findUserWithView:userView];
    UserInfo * mainUser = [self.remoteUserManager findUserWithView:self.userMainView];
    if (switchUser) {
        switchUser.userView = self.userMainView;
        if (switchUser.videoEnable) {
            [self subscribeVideo:switchUser];
        }
    } else {
        self.localUserInfo.userView = self.userMainView;
        if (self.localUserInfo.videoEnable) {
            [self reEnableVideo];
        }
    }
    if (mainUser) {
        mainUser.userView = userView;
        if (mainUser.videoEnable) {
            [self subscribeVideo:mainUser];
        }
    } else {
        self.localUserInfo.userView = userView;
        if (self.localUserInfo.videoEnable) {
            [self reEnableVideo];
        }
    }
}

- (void)switchToWhiteboard {
    if (self.layoutControl.currentPage == 0) {
        self.userMainView.hidden = YES;
        self.userTopView.hidden = YES;
        self.userLeftView.hidden = YES;
        self.userRightView.hidden = YES;
        
        [self backUserIds];
    } else {
        self.user1View.hidden = YES;
        self.user2View.hidden = YES;
        self.user3View.hidden = YES;
        self.user4View.hidden = YES;
    }
    
    self.localUserInfo.userView = self.whiteboardViewController.user1View;
    if (self.localUserInfo.videoEnable) {
        [self reEnableVideo];
    }
    UserInfo * remoteUser = [self.remoteUserManager findUserWithIndex:0];
    if (remoteUser) {
        remoteUser.userView = self.whiteboardViewController.user2View;
        if (remoteUser.videoEnable) {
            [self subscribeVideo:remoteUser];
        }
    }
    remoteUser = [self.remoteUserManager findUserWithIndex:1];
    if (remoteUser) {
        remoteUser.userView = self.whiteboardViewController.user3View;
        if (remoteUser.videoEnable) {
            [self subscribeVideo:remoteUser];
        }
    }
    remoteUser = [self.remoteUserManager findUserWithIndex:2];
    if (remoteUser) {
        remoteUser.userView = self.whiteboardViewController.user4View;
        if (remoteUser.videoEnable) {
            [self subscribeVideo:remoteUser];
        }
    }
}

- (void)switchFromWhiteboard {
    if (self.layoutControl.currentPage == 0) {
        BOOL selfIsMainUser = self.backMainUserId == self.localUserInfo.userId ? YES : NO;
        if (selfIsMainUser || self.remoteUserManager.count == 0) {
            self.localUserInfo.userView = self.userMainView;
        } else {
            self.localUserInfo.userView = self.userTopView;
        }
        if (self.localUserInfo.videoEnable) {
            [self reEnableVideo];
        }
        
        UserInfo * remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user2View];
        if (remoteUser) {
            remoteUser.userView = selfIsMainUser ? self.userTopView : self.userMainView;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
        remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user3View];
        if (remoteUser) {
            remoteUser.userView = self.userLeftView;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
        remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user4View];
        if (remoteUser) {
            remoteUser.userView = self.userRightView;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
        if (selfIsMainUser == NO) {
            remoteUser = [self.remoteUserManager findUser:self.backMainUserId];
            if (remoteUser && remoteUser.userView != self.userMainView) {
                [self switchUserToMainView:remoteUser.userView];
            }
        }
    } else {
        self.localUserInfo.userView = self.user1View;
        if (self.localUserInfo.videoEnable) {
            [self reEnableVideo];
        }
        UserInfo * remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user2View];
        if (remoteUser) {
            remoteUser.userView = self.user2View;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
        remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user3View];
        if (remoteUser) {
            remoteUser.userView = self.user3View;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
        remoteUser = [self.remoteUserManager findUserWithView:self.whiteboardViewController.user4View];
        if (remoteUser) {
            remoteUser.userView = self.user4View;
            if (remoteUser.videoEnable) {
                [self subscribeVideo:remoteUser];
            }
        }
    }
}

- (void)checkAutoStart {
    // Enable audio and video button.
    self.audioButton.enabled = YES;
    self.videoButton.enabled = YES;
    
    // The audio start automatically and mute by flag.
    [self enableAudio:YES];
    if (PanoCallClient.sharedInstance.autoMute) {
        [self muteAudio:YES];
    }
    
    // The audio dump start by flag.
    if (PanoCallClient.sharedInstance.debugMode) {
        [PanoCallClient.sharedInstance startAudioDump];
    }
    
    // The video start by flag.
    if (PanoCallClient.sharedInstance.autoVideo) {
        [self enableVideo:YES];
    }
}

- (void)addRemoteUser:(UInt64)userId withName:(NSString *)userName {
    UserInfo * addedUser = [self.remoteUserManager addUser:userId withName:userName];
    switch (self.remoteUserManager.count) {
        case 1:
            {
                if (!self.whiteboardViewController) {
                    // Switch local user to top view.
                    self.localUserInfo.userView = self.userTopView;
                    if (self.localUserInfo.videoEnable) {
                        [self reEnableVideo];
                    }
                    // Set 1st remote user to main view.
                    addedUser.userView = self.userMainView;
                } else {
                    // Set 1st remote user to whiteboard user 2 view.
                    addedUser.userView = self.whiteboardViewController.user2View;
                }
            }
            break;
            
        case 2:
            {
                // Enable layout switch.
                [self enableSwitchLayout:YES];
                if (!self.whiteboardViewController) {
                    // Set 2nd remote user to left view.
                    addedUser.userView = self.userLeftView;
                } else {
                    // Set 2st remote user to whiteboard user 3 view.
                    addedUser.userView = self.whiteboardViewController.user3View;
                }
            }
            break;
            
        case 3:
            {
                if (!self.whiteboardViewController) {
                    // Set 3rd remote user to right view or 4th view.
                    if (self.layoutControl.currentPage == 0) {
                        addedUser.userView = self.userRightView;
                    } else {
                        addedUser.userView = self.user4View;
                    }
                } else {
                    // Set 3st remote user to whiteboard user 4 view.
                    addedUser.userView = self.whiteboardViewController.user4View;
                }
            }
            break;
            
        default:
            break;
    }
}

- (void)removeRemoteUser:(UInt64)userId {
    UserInfo * removedUser = [self.remoteUserManager removeUser:userId];
    switch (self.remoteUserManager.count) {
        case 0:
            {
                if (!self.whiteboardViewController) {
                    // Current layout has been the float layout.
                    self.userTopView.hidden = YES;
                    // Switch local user to main view.
                    if (self.localUserInfo.userView != self.userMainView) {
                        self.localUserInfo.userView = self.userMainView;
                        if (self.localUserInfo.videoEnable) {
                            [self reEnableVideo];
                        }
                    }
                } else {
                    self.whiteboardViewController.user2View.hidden = YES;
                }
            }
            break;
            
        case 1:
            {
                // Disable layout switch.
                [self enableSwitchLayout:NO];
                if (!self.whiteboardViewController) {
                    // Switch to float layout.
                    if (self.layoutControl.currentPage == 1) {
                        self.layoutControl.currentPage = 0;
                        self.floatView.hidden = NO;
                        self.wallView.hidden = YES;
                        
                        self.userMainView.hidden = YES;
                        self.userTopView.hidden = YES;
                        self.userLeftView.hidden = YES;
                        self.userRightView.hidden = YES;
                        
                        BOOL selfIsMainUser = self.backMainUserId == self.localUserInfo.userId ? YES : NO;
                        self.localUserInfo.userView = selfIsMainUser ? self.userMainView : self.userTopView;
                        if (self.localUserInfo.videoEnable) {
                            [self reEnableVideo];
                        }
                        
                        UserInfo * remoteUser = [self.remoteUserManager findUserWithIndex:0];
                        remoteUser.userView = selfIsMainUser ? self.userTopView : self.userMainView;
                        if (remoteUser.videoEnable) {
                            [self subscribeVideo:remoteUser];
                        }
                    } else {
                        self.userLeftView.hidden = YES;
                        
                        if (removedUser.userView != self.userLeftView) {
                            UserInfo * remoteUser = [self.remoteUserManager findUserWithView:self.userLeftView];
                            if (remoteUser) {
                                remoteUser.userView = removedUser.userView;
                                if (remoteUser.videoEnable) {
                                    [self subscribeVideo:remoteUser];
                                }
                            } else {
                                self.localUserInfo.userView = removedUser.userView;
                                if (self.localUserInfo.videoEnable) {
                                    [self reEnableVideo];
                                }
                            }
                        }
                    }
                } else {
                    if (self.layoutControl.currentPage == 1) {
                        self.layoutControl.currentPage = 0;
                        self.floatView.hidden = NO;
                        self.wallView.hidden = YES;
                        
                        self.userMainView.hidden = YES;
                        self.userTopView.hidden = YES;
                        self.userLeftView.hidden = YES;
                        self.userRightView.hidden = YES;
                    }
                    
                    self.whiteboardViewController.user3View.hidden = YES;
                    
                    if (removedUser.userView != self.whiteboardViewController.user3View) {
                        UserInfo * remoteUser = [self.remoteUserManager findUserWithView:self.user3View];
                        if (remoteUser) {
                            remoteUser.userView = removedUser.userView;
                            if (remoteUser.videoEnable) {
                                [self subscribeVideo:remoteUser];
                            }
                        }
                    }
                }
            }
            break;
            
        case 2:
            {
                UserView * willHideView = nil;
                if (!self.whiteboardViewController) {
                    if (self.layoutControl.currentPage == 0) {
                        willHideView = self.userRightView;
                    } else {
                        willHideView = self.user4View;
                    }
                } else {
                    willHideView = self.whiteboardViewController.user4View;
                }
                willHideView.hidden = YES;
                
                if (removedUser.userView != willHideView) {
                    UserInfo * remoteUser = [self.remoteUserManager findUserWithView:willHideView];
                    if (remoteUser) {
                        remoteUser.userView = removedUser.userView;
                        if (remoteUser.videoEnable) {
                            [self subscribeVideo:remoteUser];
                        }
                    } else {
                        self.localUserInfo.userView = removedUser.userView;
                        if (self.localUserInfo.videoEnable) {
                            [self reEnableVideo];
                        }
                    }
                }
            }
            break;
            
        default:
            break;
    }
}

- (void)updateRemoteUserInfo:(UInt64)userId withAudioEnable:(BOOL)enable {
    [self.remoteUserManager findUser:userId].audioEnable = enable;
}

- (void)updateRemoteUserInfo:(UInt64)userId withAudioMute:(BOOL)mute {
    [self.remoteUserManager findUser:userId].audioMute = mute;
}

- (void)updateRemoteUserInfo:(UInt64)userId withVideoEnable:(BOOL)enable {
    UserInfo * user = [self.remoteUserManager findUser:userId];
    user.videoEnable = enable;
    if (enable) {
        [self subscribeVideo:user];
    }
}

- (void)updateRemoteUserInfo:(UInt64)userId withVideoMute:(BOOL)mute {
    [self.remoteUserManager findUser:userId].videoMute = mute;
}

- (void)setButtonImageAndTitleEdgeInsets:(UIButton *)button {
    static CGFloat space = 10.0;
    CGSize imageSize = button.imageView.frame.size;
    CGSize titleSize = button.titleLabel.frame.size;
    [button setImageEdgeInsets:UIEdgeInsetsMake(-titleSize.height - space/2.0, 0, 0, -titleSize.width)];
    [button setTitleEdgeInsets:UIEdgeInsetsMake(0, -imageSize.width, -imageSize.height - space/2.0, 0)];
}

- (void)setViewRoundedCorners:(UIView *)view withRadius:(CGFloat)radius {
    view.layer.cornerRadius = radius;
    view.layer.masksToBounds = YES;
}

- (void)backUserIds {
    UserInfo * user = [self.remoteUserManager findUserWithView:self.userMainView];
    if (user) {
        self.backMainUserId = user.userId;
    } else if (self.localUserInfo.userView == self.userMainView && self.remoteUserManager.count) {
        self.backMainUserId = self.localUserInfo.userId;
    } else {
        self.backMainUserId = 0;
    }
}

- (void)addObservers {
    // add KVO observers
    [PanoCallClient.sharedInstance addObserver:self forKeyPath:kResolutionObserverKey options:NSKeyValueObservingOptionNew|NSKeyValueObservingOptionOld context:nil];
}

- (void)removeObservers {
    // remove KVO observers
    [PanoCallClient.sharedInstance removeObserver:self forKeyPath:kResolutionObserverKey];
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
            if (self.localUserInfo.videoEnable) {
                [self reEnableVideo];
            }
        }
    }
}

@end
