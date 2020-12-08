//
//  JoinViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "JoinViewController.h"
#import "PanoCallClient.h"

static NSTimeInterval kDelayDismissAlertTime = 3.0;

@interface JoinViewController () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UINavigationItem * navigation;
@property (strong, nonatomic) IBOutlet UITextField * roomId;
@property (strong, nonatomic) IBOutlet UITextField * userName;
@property (strong, nonatomic) IBOutlet UIButton * joinButton;

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
}

- (IBAction)clickBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)clickJoin:(id)sender {
    if (self.roomId.text.length && self.userName.text.length) {
        PanoCallClient.sharedInstance.roomId = self.roomId.text;
        PanoCallClient.sharedInstance.userName = self.userName.text;
        PanoCallClient.sharedInstance.userId = [self getRandomUserId];
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

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
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
    if (PanoCallClient.sharedInstance.host) {
        self.navigation.title = NSLocalizedString(@"startCall", nil);
        [self.joinButton setTitle:NSLocalizedString(@"start", nil) forState:UIControlStateNormal];
    } else {
        self.navigation.title = NSLocalizedString(@"joinCall", nil);
        [self.joinButton setTitle:NSLocalizedString(@"join", nil) forState:UIControlStateNormal];
    }
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
    self.roomId.text = PanoCallClient.sharedInstance.host ? [self getRandomRoomId] : @"";
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

@end
