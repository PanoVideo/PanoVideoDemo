//
//  ColorButton.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "ColorButton.h"

@implementation ColorButton

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self != nil) {
        self.layer.cornerRadius = self.bounds.size.width / 2;
        self.layer.masksToBounds = YES;
        self.layer.borderWidth = 3;
        self.layer.borderColor = UIColor.clearColor.CGColor;
    }
    return self;
}

- (nullable instancetype)initWithCoder:(NSCoder *)coder {
    self = [super initWithCoder:coder];
    if (self != nil) {
        self.layer.cornerRadius = self.bounds.size.width / 2;
        self.layer.masksToBounds = YES;
        self.layer.borderWidth = 3;
        self.layer.borderColor = UIColor.clearColor.CGColor;
    }
    return self;
}

- (void)enableSlectedBorder:(BOOL)enable {
    self.layer.borderColor = enable ? UIColor.grayColor.CGColor : UIColor.clearColor.CGColor;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
