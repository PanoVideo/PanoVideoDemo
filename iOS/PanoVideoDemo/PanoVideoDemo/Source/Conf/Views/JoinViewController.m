//
//  JoinViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "JoinViewController.h"
#import "PanoCallClient.h"
#import "NSURL+Extension.h"
#import "JoinViewController.h"
#import "MBProgressHUD+Extension.h"

static NSTimeInterval kDelayDismissAlertTime = 3.0;

@interface JoinViewController () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UINavigationItem * navigation;
@property (strong, nonatomic) IBOutlet UITextField * roomId;
@property (strong, nonatomic) IBOutlet UITextField * userName;
@property (strong, nonatomic) IBOutlet UIButton * joinButton;
@property (strong, nonatomic) IBOutlet UISwitch * openAudioSwitch;
@property (strong, nonatomic) IBOutlet UISwitch * openVideoSwitch;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *headerTopConstraint;

@end

@implementation JoinViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [PanoCallClient.shared start];
    [self setTextFieldDelegate];
    [self setLocalizable];
    [self setRoomId];
    [self setUserName];
    [self setFirstResponder];
    [self setButtonRoundedCorners:self.joinButton withRadius:5.0];
    self.openAudioSwitch.on = !PanoCallClient.shared.autoMute;
    self.openVideoSwitch.on = PanoCallClient.shared.autoVideo;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (IBAction)closeTipAction:(UIButton *)sender {
    [UIView animateWithDuration:0.3 animations:^{
        self.headerTopConstraint.constant = 10;
        [sender.superview removeFromSuperview];
        [self.view layoutIfNeeded];
    } completion:^(BOOL finished) {
    }];
}


- (IBAction)switchAutoMute:(id)sender {
    PanoCallClient.shared.autoMute = !self.openAudioSwitch.on;
}

- (IBAction)switchAutoVideo:(id)sender {
    PanoCallClient.shared.autoVideo = self.openVideoSwitch.on;
}

- (IBAction)clickJoin:(id)sender {
    if (self.roomId.text.length && self.userName.text.length) {
        PanoCallClient.shared.roomId = self.roomId.text;
        PanoCallClient.shared.userName = self.userName.text;
        PanoCallClient.shared.userId = [self getRandomUserId];
            [self openCallView];
    } else {
        [self presentAlert:NSLocalizedString(@"joinAlert", nil)];
    }
}

#pragma mark - Editing

- (void)setTextFieldDelegate {
    self.roomId.delegate = self;
    self.userName.delegate = self;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    if (textField == self.roomId) {
        if ((textField.text.length >= 20) && ![string isEqualToString:@""]) {
            return NO;
        }
        return [self isInputCharAvailable:string];
    } else if (textField == self.userName) {
        if ((textField.text.length >= 20) && ![string isEqualToString:@""]) {
            return NO;
        }
    }
    return true;
}

- (BOOL)isInputCharAvailable:(NSString *)str {
    NSString *pattern = @"^[0-9a-zA-Z]*$";
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", pattern];
    BOOL isMatch = [pred evaluateWithObject:str];
    return isMatch;
}


#pragma mark - Alert

- (void)presentAlert:(NSString *)message {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle: UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil) style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
    [self performSelector:@selector(dismissAlert:) withObject:alert afterDelay:kDelayDismissAlertTime];
}

- (void)dismissAlert:(UIAlertController *)alert{
    [alert dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Private

- (void)setLocalizable {
    self.navigation.title = NSLocalizedString(@"joinCall", nil);
    [self.joinButton setTitle:NSLocalizedString(@"join", nil) forState:UIControlStateNormal];
}

- (void)openCallView {
    UIStoryboard * mainStoryboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController * viewController = [mainStoryboard instantiateViewControllerWithIdentifier:@"CallView"];
    [self showViewController:viewController sender:self];
}

- (void)openBindView {
    UIStoryboard * mainStoryboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    UIViewController * viewController = [mainStoryboard instantiateViewControllerWithIdentifier:@"BindView"];
    [self showViewController:viewController sender:self];
}

- (NSString *)getRandomRoomId {
    const static UInt64 baseRandom = 100000000;
    uint32_t randomRoomId = arc4random() % baseRandom;
    return [NSString stringWithFormat:@"%08u", randomRoomId];
}

- (UInt64)getRandomUserId {
    const static UInt64 baseRandom = 1000000;
    const static UInt64 baseId = 11 * baseRandom;
    return baseId + arc4random() % baseRandom;
}

- (void)setRoomId {
    self.roomId.keyboardType = UIKeyboardTypeASCIICapable;
    self.roomId.text = PanoCallClient.shared.roomId ?: [self getRandomRoomId];
}

- (void)setUserName {
    if (PanoCallClient.shared.userName) {
        self.userName.text = PanoCallClient.shared.userName;
    } else {
        self.userName.text = @"";
    }
}

- (void)setFirstResponder {
    if (PanoCallClient.shared.host == YES) {
        [self.userName becomeFirstResponder];
    } else {
        [self.roomId becomeFirstResponder];
    }
}

- (void)setButtonRoundedCorners:(UIButton *)button withRadius:(CGFloat)radius {
    button.layer.cornerRadius = radius;
    button.layer.masksToBounds = YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        return UIInterfaceOrientationMaskPortrait;
    }
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

- (void)joinWithRoomId:(NSString *)roomId {
    if (roomId) {
        if ([PanoCallClient shared].channelDelegate != nil) {
            // TODO 提示
            [MBProgressHUD showMessage:NSLocalizedString(@"您已经在会中...", nil) addedToView:self.presentedViewController.view duration:3];
            return;
        }
        NSTimeInterval after = 1.0;
        if (![self isViewLoaded]) {
            after = 3.0;
        }
        [self dismissViewControllerAnimated:true completion:^{}];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(after * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            self.roomId.text = roomId;
            [self clickJoin:nil];
        });
    }
}

@end
