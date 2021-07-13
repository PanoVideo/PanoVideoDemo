//
//  PanoMulticastDelegate.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class PanoMulticastDelegateEnumerator;

@interface PanoMulticastDelegate : NSObject
- (void)addDelegate:(id)delegate delegateQueue:(dispatch_queue_t)delegateQueue;
- (void)removeDelegate:(id)delegate delegateQueue:(nullable dispatch_queue_t)delegateQueue;
- (void)removeDelegate:(id)delegate;

- (void)removeAllDelegates;

@property (nonatomic, readonly) NSUInteger count;
- (NSUInteger)countOfClass:(Class)aClass;
- (NSUInteger)countForSelector:(SEL)aSelector;

- (BOOL)hasDelegateThatRespondsToSelector:(SEL)aSelector;

- (PanoMulticastDelegateEnumerator *)delegateEnumerator;

@end



@interface PanoMulticastDelegateEnumerator : NSObject

@property (nonatomic, readonly) NSUInteger count;
- (NSUInteger)countOfClass:(Class)aClass;
- (NSUInteger)countForSelector:(SEL)aSelector;

- (BOOL)getNextDelegate:(id _Nullable * _Nonnull)delPtr delegateQueue:(dispatch_queue_t _Nullable * _Nonnull)dqPtr;
- (BOOL)getNextDelegate:(id _Nullable * _Nonnull)delPtr delegateQueue:(dispatch_queue_t _Nullable * _Nonnull)dqPtr ofClass:(Class)aClass;
- (BOOL)getNextDelegate:(id _Nullable * _Nonnull)delPtr delegateQueue:(dispatch_queue_t _Nullable * _Nonnull)dqPtr forSelector:(SEL)aSelector;

@end
NS_ASSUME_NONNULL_END
