//
//  BeautifyTableViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "BeautifyTableViewController.h"
#import "PanoCallClient.h"

@interface BeautifyTableViewController ()

@property (strong, nonatomic) IBOutlet UIView * previewView;
@property (strong, nonatomic) IBOutlet UISwitch * beautifySwitch;
@property (strong, nonatomic) IBOutlet UISlider * intensitySlider;
@property (strong, nonatomic) IBOutlet UISwitch * advancedBeautifySwitch;
@property (strong, nonatomic) IBOutlet UISlider * cheekThinningSlider;
@property (strong, nonatomic) IBOutlet UISlider * eyeEnlargingSlider;

@end

@implementation BeautifyTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    [self setLocalizable];
    [self initialize];
    [self startPreview];
}

- (IBAction)clickBack:(id)sender {
    [self stopPreview];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)switchBeautify:(id)sender {
    PanoCallClient.sharedInstance.faceBeautify = self.beautifySwitch.on;
    self.intensitySlider.enabled = self.beautifySwitch.on;
    self.advancedBeautifySwitch.enabled = self.beautifySwitch.on;
    self.cheekThinningSlider.enabled = self.beautifySwitch.on && self.advancedBeautifySwitch.on;
    self.eyeEnlargingSlider.enabled = self.beautifySwitch.on && self.advancedBeautifySwitch.on;
}

- (IBAction)changeIntensity:(id)sender {
    PanoCallClient.sharedInstance.beautifyIntensity = self.intensitySlider.value;
}

- (IBAction)switchAdvancedBeautify:(id)sender {
    PanoCallClient.sharedInstance.advancedBeautify = self.advancedBeautifySwitch.on;
    self.cheekThinningSlider.enabled = self.advancedBeautifySwitch.on;
    self.eyeEnlargingSlider.enabled = self.advancedBeautifySwitch.on;
}

- (IBAction)changeCheekThinning:(id)sender {
    PanoCallClient.sharedInstance.cheekThinningIntensity = self.cheekThinningSlider.value;
}

- (IBAction)changeEyeEnlarging:(id)sender {
    PanoCallClient.sharedInstance.eyeEnlargingIntensity = self.eyeEnlargingSlider.value;
}

#pragma mark - Private

- (void)setLocalizable {
    self.title = NSLocalizedString(@"faceBeautify", nil);
}

- (void)initialize {
    self.beautifySwitch.on = PanoCallClient.sharedInstance.faceBeautify;
    self.intensitySlider.enabled = PanoCallClient.sharedInstance.faceBeautify;
    self.intensitySlider.value = PanoCallClient.sharedInstance.beautifyIntensity;
    self.advancedBeautifySwitch.enabled = PanoCallClient.sharedInstance.faceBeautify;
    self.advancedBeautifySwitch.on = PanoCallClient.sharedInstance.advancedBeautify;
    self.cheekThinningSlider.enabled = PanoCallClient.sharedInstance.advancedBeautify;
    self.cheekThinningSlider.value = PanoCallClient.sharedInstance.cheekThinningIntensity;
    self.eyeEnlargingSlider.enabled = PanoCallClient.sharedInstance.advancedBeautify;
    self.eyeEnlargingSlider.value = PanoCallClient.sharedInstance.eyeEnlargingIntensity;
}

- (void)startPreview {
    PanoRtcRenderConfig * config = [PanoRtcRenderConfig new];
    config.profileType = kPanoProfileHD720P;
    config.scalingMode = kPanoScalingCropFill;
    config.mirror = [PanoCallClient.sharedInstance.engineKit isFrontCamera];
    [PanoCallClient.sharedInstance.engineKit startPreviewWithView:self.previewView config:config];
}

- (void)stopPreview {
    [PanoCallClient.sharedInstance.engineKit stopPreview];
}

@end
