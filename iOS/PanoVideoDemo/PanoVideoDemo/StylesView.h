//
//  StylesView.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ColorButton.h"

NS_ASSUME_NONNULL_BEGIN

@interface StylesView : UIView

- (void)selectColorButton:(ColorButton *)button;
- (void)selectColor:(UIColor *)color;

@end

NS_ASSUME_NONNULL_END
