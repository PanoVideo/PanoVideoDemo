//
//  NSURL+Extension.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "NSURL+Extension.h"

@implementation NSURL (Extension)

- (NSString *)valueOf:(NSString *)queryName {
    NSURLComponents *url = [[NSURLComponents alloc] initWithString:self.absoluteString];
    if (url) {
        for (NSURLQueryItem *query in url.queryItems) {
            if ([query.name isEqualToString:queryName]) {
                return query.value;
            }
        }
    }
    return nil;
}

@end
