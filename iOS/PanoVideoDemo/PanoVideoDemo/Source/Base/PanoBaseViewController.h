//
//  PanoBaseViewController.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 控制器基类
@interface PanoBaseViewController : UIViewController

#pragma mark --模板方法
- (void)initViews;

- (void)initConstraints;

- (void)initService;

- (void)dismiss;

@end

NS_ASSUME_NONNULL_END
