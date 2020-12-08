//
//  StylesView.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "StylesView.h"

@interface StylesView ()

@property (strong, nonatomic) NSMutableArray<ColorButton *> * colorButtons;

@end

@implementation StylesView

- (void)didAddSubview:(UIView *)subview {
    if ([subview isKindOfClass:ColorButton.class]) {
        if (self.colorButtons == nil) {
            self.colorButtons = [NSMutableArray arrayWithCapacity:10];
        }
        [self.colorButtons addObject:(ColorButton *)subview];
    }
}

- (void)willRemoveSubview:(UIView *)subview {
    if ([subview isKindOfClass:ColorButton.class]) {
        [self.colorButtons removeObject:(ColorButton *)subview];
    }
}

- (void)selectColorButton:(ColorButton *)button {
    for (ColorButton * colorButton in self.colorButtons) {
        [colorButton enableSlectedBorder:[colorButton isEqual:button]];
    }
}

- (void)selectColor:(UIColor *)color {
    for (ColorButton * colorButton in self.colorButtons) {
        [colorButton enableSlectedBorder:[colorButton.backgroundColor isEqual:color]];
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
