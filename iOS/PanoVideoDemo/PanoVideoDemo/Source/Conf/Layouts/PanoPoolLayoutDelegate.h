//
//  PanoPoolLayoutDelegate.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "PanoBaseMediaView.h"

NS_ASSUME_NONNULL_BEGIN

/// 池子 布局接口
@protocol PanoPoolLayoutDelegate <NSObject>

@optional
- (void)layoutWithInfo:(NSDictionary<PanoMediaInfoKey, id> *)info;

@required
- (void)layoutWithViews:(NSArray <UIView *>*)views layoutInfo:(NSDictionary<PanoMediaInfoKey, id> * _Nullable)info;

@end

NS_ASSUME_NONNULL_END
