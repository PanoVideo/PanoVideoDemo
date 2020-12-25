//
//  JoinViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "JoinViewController.h"
#import "PanoCallClient.h"

static NSTimeInterval kDelayDismissAlertTime = 3.0;
static NSString *inputUserId = @"";
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
    self.navigation.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(showPanoInfoAlert)];
}

- (IBAction)clickBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
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

- (void)showPanoInfoAlert {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"请输入Pano配置信息" message:nil preferredStyle:UIAlertControllerStyleAlert];
    [alert addTextFieldWithConfigurationHandler:nil];
    [alert addTextFieldWithConfigurationHandler:nil];
    [alert addTextFieldWithConfigurationHandler:nil];
    [alert addTextFieldWithConfigurationHandler:nil];
    UIAlertAction *comfirmAction = [UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        inputUserId = alert.textFields[0].text;
        NSString *rtcServer = alert.textFields[1].text;
        NSString *token = alert.textFields[2].text;
        NSString *appID = alert.textFields[3].text;
        [PanoCallClient updatePanoConfigWithAppId:appID rtcServer:rtcServer token:token];
    }];
    comfirmAction.enabled = false;
    [alert addAction:comfirmAction];
    [alert addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
    NSArray *placeholders = @[@"请输入UserId", @"请输入PanoServer", @"请输入Token",@"请输入AppID"];
    for (NSInteger i=0; i<alert.textFields.count; i++) {
        UITextField *textField = alert.textFields[i];
        textField.placeholder = placeholders[i];
        [textField addTarget:self action:@selector(textFielEditingChanged:) forControlEvents:UIControlEventEditingChanged];
    }
    [self presentViewController:alert animated:true completion:^{
    }];
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
