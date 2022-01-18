//
//  PanoBaseService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Service 基类
@interface PanoBaseService<__covariant ObjectType> : NSObject

typedef void(^PanoCallBackBlock)(ObjectType __nonnull del);

- (void)initService;

#pragma mark -- 多播委托简易实现

/// 调用某个方法，调用完成后通过block 回调
- (void)invokeWithAction:(SEL)selector completion:(PanoCallBackBlock)completion;

/// 注册监听
- (void)addDelegate:(ObjectType)delegate;

/// 移除监听
- (void)removeDelegate:(ObjectType)delegate;

/// 移除所有监听
- (void)removeAllDelegates;

- (NSUInteger)delegatesCount;

@end

NS_ASSUME_NONNULL_END
