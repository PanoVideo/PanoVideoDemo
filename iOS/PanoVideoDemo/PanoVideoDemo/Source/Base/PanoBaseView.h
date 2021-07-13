//
//  PanoBaseView.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Masonry.h"
#import "PanoDefine.h"
#import "UIColor+Extension.h"

NS_ASSUME_NONNULL_BEGIN

@interface PanoBaseView : UIView

- (void)initViews;

- (void)initConstraints;

@end

NS_ASSUME_NONNULL_END
