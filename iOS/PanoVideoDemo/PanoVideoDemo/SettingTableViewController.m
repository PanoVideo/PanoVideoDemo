//
//  SettingTableViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "SettingTableViewController.h"
#import "WebViewController.h"
#import "PanoCallClient.h"

@interface SettingTableViewController () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UITextField * userName;
@property (strong, nonatomic) IBOutlet UISwitch * autoMute;
@property (strong, nonatomic) IBOutlet UISwitch * autoVideo;
@property (strong, nonatomic) IBOutlet UISegmentedControl * resolution;
@property (strong, nonatomic) IBOutlet UISwitch * autoSpeaker;
@property (strong, nonatomic) IBOutlet UISwitch * leaveConfirm;
@property (strong, nonatomic) IBOutlet UISwitch * debugMode;
@property (strong, nonatomic) IBOutlet UILabel * version;

@end

@implementation SettingTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
//     self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    [self setLocalizable];
    [self setTextFieldDelegate];
    [self initialize];
}

- (IBAction)clickBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)switchAutoMute:(id)sender {
    PanoCallClient.sharedInstance.autoMute = self.autoMute.on;
}

- (IBAction)switchAutoVideo:(id)sender {
    PanoCallClient.sharedInstance.autoVideo = self.autoVideo.on;
}

- (IBAction)switchResolution:(id)sender {
    PanoCallClient.sharedInstance.resolution = (PanoVideoProfileType)(self.resolution.selectedSegmentIndex+1);
}

- (IBAction)switchAutoSpeaker:(id)sender {
    PanoCallClient.sharedInstance.autoSpeaker = self.autoSpeaker.on;
}

- (IBAction)switchLeaveConfirm:(id)sender {
    PanoCallClient.sharedInstance.leaveConfirm = self.leaveConfirm.on;
}

- (IBAction)switchDebugMode:(id)sender {
    if (self.debugMode.on) {
        [self presentDebugModeAlert];
    } else {
        PanoCallClient.sharedInstance.debugMode = self.debugMode.on;
    }
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
        PanoCallClient.sharedInstance.userName = self.userName.text;
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
        PanoCallClient.sharedInstance.debugMode = self.debugMode.on;
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
    if (PanoCallClient.sharedInstance.userName.length) {
        self.userName.text = PanoCallClient.sharedInstance.userName;
    } else {
        self.userName.text = @"";
    }
    
    self.autoMute.on = PanoCallClient.sharedInstance.autoMute;
    self.autoVideo.on = PanoCallClient.sharedInstance.autoVideo;
    NSUInteger index = PanoCallClient.sharedInstance.resolution - 1;
    if (index >= 0 && index <= 4) {
        self.resolution.selectedSegmentIndex = index;
    }
    self.autoSpeaker.on = PanoCallClient.sharedInstance.autoSpeaker;
    self.leaveConfirm.on = PanoCallClient.sharedInstance.leaveConfirm;
    
    self.debugMode.on = PanoCallClient.sharedInstance.debugMode;
    
    self.version.text = PanoCallClient.productVersion;
}

- (IBAction)showEnvrionmentAlert:(id)sender { }

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAllButUpsideDown;
}
@end
