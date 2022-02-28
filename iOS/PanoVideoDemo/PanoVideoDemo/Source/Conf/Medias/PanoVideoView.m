//
//  PanoVideoView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoVideoView.h"
#import "PanoCallClient.h"

@interface PanoVideoView ()

@property (strong, nonatomic) UIView *bottomView;

@end
@implementation PanoVideoView

- (void)initViews {
    [super initViews];
    
    self.layer.cornerRadius = 5;
    self.layer.masksToBounds = true;
    
    _bottomView = [[UIView alloc] init];
    _bottomView.layer.cornerRadius = 2;
    _bottomView.layer.masksToBounds = true;
    _bottomView.backgroundColor = [[UIColor pano_colorWithHexString:@"111111"] colorWithAlphaComponent:0.6];
    [self.contentView addSubview:_bottomView];
    
    _userName = [[UILabel alloc] init];
    _userName.textAlignment = NSTextAlignmentRight;
    _userName.textColor = [UIColor whiteColor];
    _userName.lineBreakMode = NSLineBreakByTruncatingMiddle;
    [_userName setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
    [_bottomView addSubview:_userName];
    
    _micIcon = [[UIImageView alloc] init];
    [_micIcon sizeToFit];
    [_bottomView addSubview:_micIcon];
    
    _signalView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"network_signal_good"]];
    _signalView.contentMode = UIViewContentModeScaleAspectFit;
    [_signalView sizeToFit];
    [_bottomView addSubview:_signalView];
    
    
    UIImage *avator = [UIImage imageNamed:@"image.video.avatar"];
    _avatarImageView = [[UIImageView alloc] initWithImage:avator];
    [_avatarImageView sizeToFit];
    _avatarImageView.hidden = true;
    [self insertSubview:_avatarImageView aboveSubview:self.contentView];
}

- (void)initConstraints {
    [super initConstraints];
    
    [_bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(self);
        make.height.mas_equalTo(25);
        make.bottom.mas_equalTo(self).mas_offset(-20);
        make.width.lessThanOrEqualTo(self);
    }];
    
    [_userName mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(_bottomView).mas_offset(5);
        make.centerY.mas_equalTo(_bottomView);
        make.width.mas_lessThanOrEqualTo(100);
    }];
    
    [_micIcon mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(_signalView.mas_left).mas_offset(-5);
        make.width.height.mas_equalTo(12);
        make.left.mas_equalTo(_userName.mas_right).mas_offset(5);
        make.centerY.mas_equalTo(_bottomView);
    }];
    
    [_signalView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(_bottomView).mas_offset(-5);
        make.width.mas_equalTo(15);
        make.height.mas_equalTo(12);
        make.centerY.mas_equalTo(_bottomView);
    }];
    
    [_avatarImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(self);
    }];
}

- (UIImage *)micImage {
    return self.instance.user.audioImage;
}

- (UIImage *)avatarImage {
    return [UIImage imageNamed:@"image.avatar.big"];
}

- (void)setActive:(BOOL)active {
    [super setActive:active];
    self.layer.borderWidth = active ? 3 : 0.0;
    self.layer.borderColor = [UIColor pano_colorWithHexString:@"68d56a"].CGColor;
}

- (void)start {
    [self update];
    PanoUserInfo * user = self.instance.user;
    _avatarImageView.hidden = user.videoStaus == PanoUserVideo_Unmute;
    if (user.videoStaus == PanoUserVideo_Unmute) {
        [PanoCallClient.shared.video startVideoWithView:self.contentView instance:self.instance];
    } else if (user.videoStaus == PanoUserVideo_Mute || user.videoStaus == PanoUserVideo_None) {
        [self stop];
    }
}

- (void)stop {
    [PanoCallClient.shared.video unsubscribe:self.instance.user.userId];
    [self update];
}

- (void)update {
    _userName.text = self.instance.user.videoUserName;
    _micIcon.image = [self micImage];
    _avatarImageView.image = [self avatarImage];
    if (self.instance.mode == PanoViewInstance_Avg||
        self.instance.mode == PanoViewInstance_Float) {
        self.contentView.backgroundColor = [UIColor pano_colorWithHexString:@"555555"];
        [_userName setFont:[UIFont systemFontOfSize:PanoFontSize_Little]];
        [_bottomView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.mas_equalTo(self).mas_offset(0);
        }];
        [_avatarImageView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(30);
        }];
    } else if (self.instance.mode == PanoViewInstance_Max) {
        self.contentView.backgroundColor = [UIColor pano_colorWithHexString:@"111111"];
        [_userName setFont:[UIFont systemFontOfSize:PanoFontSize_Medium]];
        [_bottomView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.mas_equalTo(self).mas_offset(-20);
        }];
        [_avatarImageView mas_updateConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(60);
        }];
    }
}

- (void)update:(NSDictionary<PanoMediaInfoKey,id> *)info {
    if (info[PanoNetworkStatus]) {
        PanoQualityRating rating = [info[PanoNetworkStatus] integerValue];
        if (rating == kPanoQualityGood || rating == kPanoQualityExcellent) {
            _signalView.image = [UIImage imageNamed:@"network_signal_good"];
        } else if (rating == kPanoQualityPoor) {
            _signalView.image = [UIImage imageNamed:@"network_signal_poor"];
        } else {
            _signalView.image = [UIImage imageNamed:@"network_signal_bad"];
        }
    }
}


@end
