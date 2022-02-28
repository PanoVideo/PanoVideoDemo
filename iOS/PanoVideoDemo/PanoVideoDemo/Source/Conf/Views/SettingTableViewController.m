//
//  SettingTableViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "SettingTableViewController.h"
#import "WebViewController.h"
#import "PanoCallClient.h"


@interface SettingTableViewController () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UITextField * userName;
@property (strong, nonatomic) IBOutlet UISwitch * staticsFlag;
@property (strong, nonatomic) IBOutlet UISwitch * autoVideo;
@property (strong, nonatomic) IBOutlet UISegmentedControl * resolution;
@property (strong, nonatomic) IBOutlet UISwitch * autoSpeaker;
@property (strong, nonatomic) IBOutlet UISwitch * leaveConfirm;
@property (strong, nonatomic) IBOutlet UISwitch * debugMode;
@property (strong, nonatomic) IBOutlet UILabel * version;
@property (weak, nonatomic) IBOutlet UISegmentedControl *frameControl;
@property (weak, nonatomic) IBOutlet UILabel *shareOptionLabel;
@end

@implementation SettingTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setLocalizable];
    [self setTextFieldDelegate];
    [self initialize];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    PanoConfig *config = PanoCallClient.shared.config;
    self.shareOptionLabel.text = config.shareOptions[config.shareOption];
}

- (IBAction)clickBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)switchStaticsFlag:(id)sender {
    PanoCallClient.shared.staticsFlag = self.staticsFlag.on;
}

- (IBAction)switchAutoVideo:(id)sender {
    PanoCallClient.shared.autoVideo = self.autoVideo.on;
}

- (IBAction)switchResolution:(id)sender {
    PanoCallClient.shared.resolution = (PanoVideoProfileType)(self.resolution.selectedSegmentIndex+1);
}

- (IBAction)switchAutoSpeaker:(id)sender {
    PanoCallClient.shared.autoSpeaker = self.autoSpeaker.on;
}

- (IBAction)switchLeaveConfirm:(id)sender {
    PanoCallClient.shared.leaveConfirm = self.leaveConfirm.on;
}

- (IBAction)switchDebugMode:(id)sender {
    if (self.debugMode.on) {
        [self presentDebugModeAlert];
    } else {
        PanoCallClient.shared.debugMode = self.debugMode.on;
    }
}

- (IBAction)switchFrame:(UISegmentedControl *)sender {
    PanoVideoFrameRateType type = (PanoVideoFrameRateType)sender.selectedSegmentIndex;
    PanoCallClient.shared.frameRate = type;
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier compare:@"Feedback"] == NSOrderedSame) {
        UINavigationController * feedbackNavigation = segue.destinationViewController;
        feedbackNavigation.topViewController.title = NSLocalizedString(@"feedback", nil);
    } else if ([segue.identifier compare:@"Help"] == NSOrderedSame) {
        UINavigationController * webNavigation = segue.destinationViewController;
        WebViewController * webViewController = (WebViewController *)webNavigation.topViewController;
        webViewController.title = NSLocalizedString(@"help", nil);
        webViewController.webUrl = @"https://developer.pano.video/getting-started/intro/";
    } else if ([segue.identifier compare:@"About"] == NSOrderedSame) {
        UINavigationController *webNavigation = segue.destinationViewController;
        WebViewController * webViewController = (WebViewController *)webNavigation.topViewController;
        webViewController.title = NSLocalizedString(@"about", nil);
        webViewController.webUrl = @"https://www.pano.video/about.html";
    }
}

#pragma mark - Editing

- (void)setTextFieldDelegate {
    self.userName.delegate = self;
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    if (textField == self.userName) {
        PanoCallClient.shared.userName = self.userName.text;
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

#pragma mark - Alert

- (void)presentDebugModeAlert {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:@""
                                                                    message:NSLocalizedString(@"debugAlert", nil)
                                                             preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction * ok = [UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil)
                                                  style:UIAlertActionStyleDefault
                                                handler:^(UIAlertAction * _Nonnull action) {
        PanoCallClient.shared.debugMode = self.debugMode.on;
    }];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"cancel", nil)
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        self.debugMode.on = NO;
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - Private

- (void)setLocalizable {
    self.title = NSLocalizedString(@"setting", nil);
}

- (void)initialize {
    if (PanoCallClient.shared.userName.length) {
        self.userName.text = PanoCallClient.shared.userName;
    } else {
        self.userName.text = @"";
    }
    
    self.staticsFlag.on = PanoCallClient.shared.staticsFlag;
    self.autoVideo.on = PanoCallClient.shared.autoVideo;
    NSUInteger index = PanoCallClient.shared.resolution - 1;
    if (index >= 0 && index <= 4) {
        self.resolution.selectedSegmentIndex = index;
    }
    self.autoSpeaker.on = PanoCallClient.shared.autoSpeaker;
    self.leaveConfirm.on = PanoCallClient.shared.leaveConfirm;
    
    self.debugMode.on = PanoCallClient.shared.debugMode;
    
    self.version.text = PanoCallClient.productVersion;
    self.frameControl.selectedSegmentIndex = PanoCallClient.shared.frameRate;
}

- (IBAction)showEnvrionmentAlert:(UITapGestureRecognizer *)sender {
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 1 && ![PanoCallClient.shared.userMgr isHost]) {
        return 4;
    }
    return [super tableView:tableView numberOfRowsInSection:section];
}
@end
