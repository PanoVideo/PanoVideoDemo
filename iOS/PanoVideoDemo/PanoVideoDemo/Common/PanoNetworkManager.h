//
//  PanoNetworkManager.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^PanoRequestCompletion)(NSError * _Nullable error, NSDictionary *_Nullable result);


@interface PanoNetworkManager : NSObject

+ (void)getWithURL:(NSString *)urlString
                params:(NSDictionary *)params
            completion:(PanoRequestCompletion)completion;

+ (void)postWithURL:(NSString *)urlString
             params:(NSDictionary *)params
         completion:(PanoRequestCompletion)completion;

+ (void)postWithURL:(NSString *)urlString
             params:(NSDictionary *)params
             header:(NSDictionary<NSString *, NSString *> *)header
         completion:(PanoRequestCompletion)completion;

+ (NSString *)generatePanoSignWithParams:(NSDictionary *)params;

@end

NS_ASSUME_NONNULL_END
