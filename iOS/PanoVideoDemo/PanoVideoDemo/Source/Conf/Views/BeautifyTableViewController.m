//
//  BeautifyTableViewController.m
//  PanoVideoDemo
//
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
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)switchBeautify:(id)sender {
    PanoCallClient.shared.faceBeautify = self.beautifySwitch.on;
    self.intensitySlider.enabled = self.beautifySwitch.on;
    self.advancedBeautifySwitch.enabled = self.beautifySwitch.on;
    self.cheekThinningSlider.enabled = self.beautifySwitch.on && self.advancedBeautifySwitch.on;
    self.eyeEnlargingSlider.enabled = self.beautifySwitch.on && self.advancedBeautifySwitch.on;
}

- (IBAction)changeIntensity:(id)sender {
    PanoCallClient.shared.beautifyIntensity = self.intensitySlider.value;
}

- (IBAction)switchAdvancedBeautify:(id)sender {
    PanoCallClient.shared.advancedBeautify = self.advancedBeautifySwitch.on;
    self.cheekThinningSlider.enabled = self.advancedBeautifySwitch.on;
    self.eyeEnlargingSlider.enabled = self.advancedBeautifySwitch.on;
}

- (IBAction)changeCheekThinning:(id)sender {
    PanoCallClient.shared.cheekThinningIntensity = self.cheekThinningSlider.value;
}

- (IBAction)changeEyeEnlarging:(id)sender {
    PanoCallClient.shared.eyeEnlargingIntensity = self.eyeEnlargingSlider.value;
}

#pragma mark - Private

- (void)setLocalizable {
    self.title = NSLocalizedString(@"faceBeautify", nil);
}

- (void)initialize {
    self.beautifySwitch.on = PanoCallClient.shared.faceBeautify;
    self.intensitySlider.enabled = PanoCallClient.shared.faceBeautify;
    self.intensitySlider.value = PanoCallClient.shared.beautifyIntensity;
    self.advancedBeautifySwitch.enabled = PanoCallClient.shared.faceBeautify;
    self.advancedBeautifySwitch.on = PanoCallClient.shared.advancedBeautify;
    self.cheekThinningSlider.enabled = PanoCallClient.shared.advancedBeautify;
    self.cheekThinningSlider.value = PanoCallClient.shared.cheekThinningIntensity;
    self.eyeEnlargingSlider.enabled = PanoCallClient.shared.advancedBeautify;
    self.eyeEnlargingSlider.value = PanoCallClient.shared.eyeEnlargingIntensity;
}

- (void)startPreview {
    PanoRtcRenderConfig * config = [PanoRtcRenderConfig new];
    config.profileType = kPanoProfileHD720P;
    config.scalingMode = kPanoScalingCropFill;
    config.mirror = [PanoCallClient.shared.engineKit isFrontCamera];
    [PanoCallClient.shared.engineKit startPreviewWithView:self.previewView config:config];
}

- (void)stopPreview {
    [PanoCallClient.shared.engineKit stopPreview];
}

- (void)dealloc {
    [self stopPreview];
}
@end
