//
//  UserView.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "UserView.h"

#pragma mark - UserStatusView

@interface UserStatusView : UIView

@end

@implementation UserStatusView

@end

#pragma mark - UserView

@implementation UserView

- (void)didAddSubview:(UIView *)subview {
    if ([subview isKindOfClass:UserStatusView.class]) {
        for (UIView * view in subview.subviews) {
            if ([view isKindOfClass:UILabel.class]) {
                _userName = (UILabel *)view;
//                NSLog(@"add user name: %@ with %@", _userName.text, _userName);
            } else if ([view isKindOfClass:UIImageView.class]) {
                _micIcon = (UIImageView *)view;
//                NSLog(@"add mic icon: %@ with %@", _micIcon.image, _micIcon);
            }
        }
    }
}

- (void)willRemoveSubview:(UIView *)subview {
    if ([subview isKindOfClass:UserStatusView.class]) {
        _userName = nil;
        _micIcon = nil;
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
