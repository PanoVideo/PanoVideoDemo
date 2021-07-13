//
//  PanoBaseService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PanoBaseService<__covariant ObjectType> : NSObject

typedef void(^PanoCallBackBlock)(ObjectType __nonnull del);

- (void)initService;

- (void)invokeWithAction:(SEL)selector completion:(PanoCallBackBlock)completion;

- (void)addDelegate:(ObjectType)delegate;

- (void)removeDelegate:(ObjectType)delegate;

- (void)removeAllDelegates;

@end

NS_ASSUME_NONNULL_END
