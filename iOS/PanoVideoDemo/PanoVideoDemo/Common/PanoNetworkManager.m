//
//  PanoNetworkManager.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoNetworkManager.h"
#import <CommonCrypto/CommonHMAC.h>
#import "PanoCallClient.h"
static NSString *const AppDemoSecret = @"pvvd68978e0c4fb39654b66bcbbc46cb";

@implementation PanoNetworkManager


+ (void)requestWithMethod:(NSString *)method
               requestURL:(NSString *)urlString
                   params:(NSDictionary *)params
                   header:(NSDictionary<NSString *, NSString *> *)header
               completion:(PanoRequestCompletion)completion
{
    NSURL *requestURL = [NSURL URLWithString:urlString];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:requestURL];
    [request setValue:NSUUID.UUID.UUIDString forHTTPHeaderField:@"Tracking-Id"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"no-cache" forHTTPHeaderField:@"Cache-Control"];
    [request setValue:@"Keep-Alive" forHTTPHeaderField:@"Connection"];
    for (NSString *key in header.allKeys) {
        [request setValue:header[key] forHTTPHeaderField:key];
    }
    request.HTTPMethod = method;
    request.timeoutInterval = 60;

    NSError * error = nil;
    request.HTTPBody = [NSJSONSerialization dataWithJSONObject:params options:NSJSONWritingPrettyPrinted error:&error];
    if (nil != error) {
        completion(false, nil);
        return;
    }
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
        if (200 == httpResponse.statusCode) {
            NSError *error = nil;
            NSDictionary * jsonData = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(nil, jsonData);
            });
        } else {
            if (!error) {
                error = [NSError errorWithDomain:@"com.pano.video" code:httpResponse.statusCode userInfo:nil];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(error, nil);
            });
        }
    }];
    [dataTask resume];
}

+ (NSString *)generatePanoSignWithParams:(NSDictionary *)params {
    NSString *randomNumber = [self getRandomNumber];
    NSUInteger timestamp = [[NSDate date] timeIntervalSince1970];
    NSString *jsonParams = @"";
    if (params.count > 0) {
        @try {
            NSError *error = nil;
            NSData *data = [NSJSONSerialization dataWithJSONObject:params options:kNilOptions error:&error];
            jsonParams = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        } @catch (NSException *exception) {
            
        }
    }
    NSString *value = [NSString stringWithFormat:@"%lu%@",(unsigned long)timestamp,jsonParams];
    NSString *signature = [self hashedValue:AppDemoSecret andData:value];
    NSString *sign = [NSString stringWithFormat:@"%@%lu%@",randomNumber,(unsigned long)timestamp, signature];
    return sign;
}

+ (NSString *)getRandomNumber {
    const static UInt64 baseID = 1000000;
    uint32_t randomRoomId = arc4random() % baseID;
    return [NSString stringWithFormat:@"%06u", randomRoomId];
}

+ (NSString *)hashedValue:(NSString *)key andData:(NSString *)data {
    const char *cKey  = [key cStringUsingEncoding:NSUTF8StringEncoding];
    const char *cData = [data cStringUsingEncoding:NSUTF8StringEncoding];
    unsigned char cHMAC[CC_SHA256_DIGEST_LENGTH];
    CCHmac(kCCHmacAlgSHA256, cKey, strlen(cKey), cData, strlen(cData), cHMAC);
    NSData *HMAC = [[NSData alloc] initWithBytes:cHMAC
                                          length:sizeof(cHMAC)];
    NSString *hash = [HMAC base64EncodedStringWithOptions:0];
    return hash;
    
}

+ (void)getWithURL:(NSString *)urlString
                params:(NSDictionary *)params
        completion:(PanoRequestCompletion)completion {
    [self requestWithMethod:@"GET" requestURL:urlString params:params header:nil completion:completion];
}

+ (void)postWithURL:(NSString *)urlString
             params:(NSDictionary *)params
         completion:(PanoRequestCompletion)completion {
    [self requestWithMethod:@"POST" requestURL:urlString params:params header:nil completion:completion];
}

+ (void)postWithURL:(NSString *)urlString
             params:(NSDictionary *)params
             header:(NSDictionary<NSString *, NSString *> *)header
         completion:(PanoRequestCompletion)completion {
    [self requestWithMethod:@"POST" requestURL:urlString params:params header:header completion:completion];
}

@end
