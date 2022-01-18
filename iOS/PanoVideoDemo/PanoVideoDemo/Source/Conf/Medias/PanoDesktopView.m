//
//  PanoDesktopView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoDesktopView.h"
#import "PanoCallClient.h"

@interface PanoDesktopView()
@property (strong, nonatomic) UIView *bottomView;

@end

@implementation PanoDesktopView

- (void)initViews {
    [super initViews];
    
    _bottomView = [[UIView alloc] init];
    _bottomView.layer.cornerRadius = 2;
    _bottomView.layer.masksToBounds = true;
    _bottomView.backgroundColor = [[UIColor pano_colorWithHexString:@"111111"] colorWithAlphaComponent:0.6];
    [self.contentView addSubview:_bottomView];
    
    _userName = [[UILabel alloc] init];
    _userName.textAlignment = NSTextAlignmentRight;
    _userName.textColor = [UIColor whiteColor];
    [_userName setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
    [_bottomView addSubview:_userName];
    
}

- (void)initConstraints {
    [super initConstraints];
    
    [_bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(self);
        make.height.mas_equalTo(30);
        make.bottom.mas_equalTo(self).mas_offset(-20);
    }];
    
    [_userName mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(_bottomView).insets(UIEdgeInsetsMake(0, DefaultFixSpace, 0, DefaultFixSpace));
        make.centerY.mas_equalTo(_bottomView);
    }];
}

- (void)start {
    PanoUserInfo * user = self.instance.user;
    if (user.screenStatus == PanoUserScreen_Unmute) {
        [PanoCallClient.shared.screen subscribe:user.userId WithView:self.contentView];
    }
    _userName.text = [NSString stringWithFormat:NSLocalizedString(@"%@’s screen", ),self.instance.user.userName];
    [_userName sizeToFit];
}

- (void)stop {
    PanoUserInfo * user = self.instance.user;
    [PanoCallClient.shared.screen unsubscribe:user.userId];
}

- (void)dealloc {
    NSLog(@"stopAnnotation delloc");
}

@end
