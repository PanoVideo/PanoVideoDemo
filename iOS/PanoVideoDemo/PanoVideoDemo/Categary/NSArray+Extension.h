//
//  NSArray+Extension.h
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSArray<ObjectType> (Extension)

typedef id _Nullable (^MapBlock)(ObjectType object);
typedef id _Nullable (^MapBlockWithIndex)(ObjectType object, NSUInteger idx, BOOL *stop);

- (NSArray<ObjectType> *)filteredArrayUsingBlock:(BOOL (^)(ObjectType obj, NSUInteger idx, BOOL *stop))predicate;

- (NSArray<ObjectType> *)arrayByMappingObjectsUsingBlock:(MapBlock)block;

- (NSArray<ObjectType> *)arrayByMappingObjectsUsingBlockWithIndex:(MapBlockWithIndex)blockWithIndex;

@end


@interface NSMutableArray <ObjectType>(Extension)

- (void)removeObjectWithPredicate:(BOOL (^)(ObjectType obj, NSUInteger idx, BOOL *stop))predicate;

- (void)filterArrayUsingBlock:(BOOL (^)(id obj, NSUInteger idx, BOOL *stop))predicate;
@end
NS_ASSUME_NONNULL_END
