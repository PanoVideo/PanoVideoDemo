//
//  MainViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "MainViewController.h"
#import "PanoCallClient.h"


@interface MainViewController ()

@property (strong, nonatomic) IBOutlet UIButton * startCallButton;
@property (strong, nonatomic) IBOutlet UIButton * joinCallButton;

@property (assign, nonatomic) BOOL versionChecked;
@property (assign, nonatomic) NSTimeInterval versionCheckTime;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setButtonImageAndTitleEdgeInsets:self.startCallButton];
    [self setButtonImageAndTitleEdgeInsets:self.joinCallButton];
    
    self.versionChecked = NO;
    self.versionCheckTime = 0;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier compare:@"StartCall"] == NSOrderedSame) {
        PanoCallClient.sharedInstance.host = YES;
    } else if ([segue.identifier compare:@"JoinCall"] == NSOrderedSame) {
        PanoCallClient.sharedInstance.host = NO;
    }
}

#pragma mark - Private

- (void)setButtonImageAndTitleEdgeInsets:(UIButton *)button {
    static CGFloat space = 20.0;
    CGSize imageSize = button.imageView.frame.size;
    CGSize titleSize = button.titleLabel.frame.size;
    [button setImageEdgeInsets:UIEdgeInsetsMake(-titleSize.height - space/2.0, 0, 0, -titleSize.width)];
    [button setTitleEdgeInsets:UIEdgeInsetsMake(0, -imageSize.width, -imageSize.height - space/2.0, 0)];
}

@end
