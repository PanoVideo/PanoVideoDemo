//
//  UIView+Extension.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "UIView+Extension.h"

@implementation UIView (Extension)

- (void)pano_autoLayout {
    [UIView animateWithDuration:0.3 animations:^{
        [self setNeedsLayout];
        [self updateConstraintsIfNeeded];
        [self layoutIfNeeded];
    }];
}

@end
