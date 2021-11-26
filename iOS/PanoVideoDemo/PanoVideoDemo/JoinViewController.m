//
//  JoinViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "JoinViewController.h"
#import "PanoCallClient.h"
#import "PanoClientService.h"
#import "PanoServiceManager.h"
#import "NSURL+Extension.h"
#import "JoinViewController.h"
#import "PanoCallClient.h"
#import "MBProgressHUD+Extension.h"

static NSTimeInterval kDelayDismissAlertTime = 3.0;
static NSString *inputUserId = @"";
@interface JoinViewController () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UINavigationItem * navigation;
@property (strong, nonatomic) IBOutlet UITextField * roomId;
@property (strong, nonatomic) IBOutlet UITextField * userName;
@property (strong, nonatomic) IBOutlet UIButton * joinButton;
@property (strong, nonatomic) IBOutlet UISwitch * autoMute;
@property (strong, nonatomic) IBOutlet UISwitch * autoVideo;
@end

@implementation JoinViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setTextFieldDelegate];
    [self setLocalizable];
    [self setRoomId];
    [self setUserName];
    [self setFirstResponder];
    [self setButtonRoundedCorners:self.joinButton withRadius:5.0];
    self.autoMute.on = PanoCallClient.sharedInstance.autoMute;
    self.autoVideo.on = PanoCallClient.sharedInstance.autoVideo;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [PanoClientService checkVersion];
}


- (IBAction)switchAutoMute:(id)sender {
    PanoCallClient.sharedInstance.autoMute = self.autoMute.on;
}

- (IBAction)switchAutoVideo:(id)sender {
    PanoCallClient.sharedInstance.autoVideo = self.autoVideo.on;
}

- (IBAction)clickJoin:(id)sender {
    if (self.roomId.text.length && self.userName.text.length) {
        PanoCallClient.sharedInstance.roomId = self.roomId.text;
        PanoCallClient.sharedInstance.userName = self.userName.text;
        PanoCallClient.sharedInstance.userId = inputUserId.length > 0 ?
                                               inputUserId.integerValue : [self getRandomUserId];
        if (PanoCallClient.sharedInstance.mobileNumber.length != 0) {
            [self openCallView];
        } else {
            [self checkLocation];
        }
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

- (void)textFielEditingChanged:(UITextField *)textField {
    UIAlertController *alert = (UIAlertController *)self.presentedViewController;
    if ([alert isKindOfClass:[UIAlertController class]]) {
        BOOL enable = true;
        for (NSInteger i=0; i<alert.textFields.count; i++) {
            UITextField *textField = alert.textFields[i];
            if (textField.text.length <= 0) {
                enable = false;
            }
        }
        alert.actions.firstObject.enabled = enable;
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    if (textField == self.roomId) {
        if ((textField.text.length >= 20) && ![string isEqualToString:@""]) {
            return NO;
        }
        if ([self isInputCharAvailable:string]) {
            return true;
        }
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
//    if (PanoCallClient.sharedInstance.host) {
//        self.navigation.title = NSLocalizedString(@"startCall", nil);
//        [self.joinButton setTitle:NSLocalizedString(@"start", nil) forState:UIControlStateNormal];
//    } else {
        self.navigation.title = NSLocalizedString(@"joinCall", nil);
        [self.joinButton setTitle:NSLocalizedString(@"join", nil) forState:UIControlStateNormal];
//    }
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

- (void)checkLocation {
    [self openCallView];
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
    self.roomId.text = PanoCallClient.sharedInstance.roomId ?: [self getRandomRoomId];
}

- (void)setUserName {
    if (PanoCallClient.sharedInstance.userName) {
        self.userName.text = PanoCallClient.sharedInstance.userName;
    } else {
        self.userName.text = @"";
    }
}

- (void)setFirstResponder {
    if (PanoCallClient.sharedInstance.host == YES) {
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
@end
