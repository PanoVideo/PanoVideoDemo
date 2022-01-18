//
//  PanoViewInstance.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoViewInstance.h"
#import "PanoCallClient.h"

@implementation PanoViewInstance

- (BOOL)isEqual:(id)object {
    if (![object isKindOfClass:[PanoViewInstance class]]) {
        return false;
    }
    // 类型一致 userId 一致
    return self.type == ((PanoViewInstance *)object).type && (self.user.userId == ((PanoViewInstance *)object).user.userId);
}

- (PanoUserInfo *)user {
    return [PanoCallClient.shared.userMgr findUserWithId:_userId];
}

- (NSString *)description {
    return [NSString stringWithFormat:@"_type:%ld user: %@",(long)_mode, self.user];
}

- (instancetype)initWithUserId:(UInt64)userId type:(PanoViewInstanceType)type {
    self = [super init];
    if (self) {
        _userId = userId;
        _type = type;
    }
    return self;
}

@end


@implementation PanoViewPage

- (NSString *)description {
    return [NSString stringWithFormat:@"_instances:%@ _type: %ld",_instances, (long)_type];
}

- (BOOL)isEqual:(PanoViewPage *)object {
    if (![object isKindOfClass:[PanoViewPage class]]) {
        return false;
    }
    return self.type == object.type && [self.instances isEqualToArray:object.instances];
}
@end


@implementation PanoAnnotationItem

- (instancetype)initWithUserId:(UInt64)userId
                          type:(PanoViewInstanceType)type
                      streamId:(SInt32)streamId {
    self = [self initWithUserId:userId type:type];
    if (self) {
        self.streamId = streamId;
    }
    return self;
}

- (instancetype)initWithUserId:(UInt64)userId type:(PanoViewInstanceType)type {
    self = [super init];
    if (self) {
        _userId = userId;
        _type = type;
    }
    return self;
}

- (BOOL)isEqual:(PanoAnnotationItem *)object {
    if (![object isKindOfClass:[PanoAnnotationItem class]]) {
        return false;
    }
    return self.type == ((PanoAnnotationItem *)object).type && (self.userId == ((PanoAnnotationItem *)object).userId);
}

@end
