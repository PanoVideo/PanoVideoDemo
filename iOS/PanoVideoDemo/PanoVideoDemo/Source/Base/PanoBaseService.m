//
//  PanoBaseService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoMulticastDelegate.h"

@interface PanoBaseService ()
@property (nonatomic, strong) PanoMulticastDelegate *delegates;
@end

@implementation PanoBaseService

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self initService];
    }
    return self;
}

- (void)initService {
    
}

- (PanoMulticastDelegate *)delegates {
    if (!_delegates) {
        _delegates = [[PanoMulticastDelegate alloc] init];
    }
    return _delegates;
}

- (NSUInteger)delegatesCount {
    return self.delegates.count;
}

- (void)addDelegate:(id)delegate {
    [self.delegates addDelegate:delegate delegateQueue:dispatch_get_main_queue()];
}

- (void)removeDelegate:(id)delegate {
    [self.delegates removeDelegate:delegate];
}

- (void)removeAllDelegates {
    [self.delegates removeAllDelegates];
}

- (void)invokeWithAction:(SEL)selector completion:(PanoCallBackBlock)completion {
    PanoMulticastDelegateEnumerator *delegateEnum = self.delegates.delegateEnumerator;
    id del;
    dispatch_queue_t dq;
    while ([delegateEnum getNextDelegate:&del delegateQueue:&dq forSelector:selector]) {
        dispatch_async(dq, ^{
            if (completion) {
                completion(del);
            }
        });
    }
}

- (void)dealloc {
    NSLog(@"dealloc: %@", self);
    [self removeAllDelegates];
}
@end
