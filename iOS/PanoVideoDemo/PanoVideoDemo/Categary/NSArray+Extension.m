//
//  NSArray+Extension.m
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "NSArray+Extension.h"

@implementation NSArray (Extension)

- (NSArray *)filteredArrayUsingBlock:(BOOL (^)(id obj, NSUInteger idx, BOOL *stop))predicate {
    NSIndexSet * filteredIndexes = [self indexesOfObjectsPassingTest:predicate];
    return [self objectsAtIndexes:filteredIndexes];
}

- (NSArray *)arrayByMappingObjectsUsingBlock:(MapBlock)block {
    NSMutableArray *resultArray = [[NSMutableArray alloc] init];
    for( id obj in self ) {
        [resultArray addObject:block(obj)];
    }
    return resultArray;
}

- (NSArray *)arrayByMappingObjectsUsingBlockWithIndex:(id(^)(id, NSUInteger, BOOL *))blockWithIndex
{
    NSMutableArray *result = [NSMutableArray arrayWithCapacity:[self count]];
    [self enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [result addObject:blockWithIndex(obj, idx, stop)];
    }];
    return result;
}

@end

@implementation NSMutableArray (Extension)

- (void)removeObjectWithPredicate:(BOOL (^)(id _Nonnull, NSUInteger, BOOL * _Nonnull))predicate {
    NSIndexSet * filteredIndexes = [self indexesOfObjectsPassingTest:predicate];
    [self removeObjectsAtIndexes:filteredIndexes];
}

- (void)filterArrayUsingBlock:(BOOL (^)(id _Nonnull, NSUInteger, BOOL * _Nonnull))predicate {
    NSIndexSet * filteredIndexes = [self indexesOfObjectsPassingTest:^BOOL(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        return !predicate(obj, idx, stop);
    }];
    [self removeObjectsAtIndexes:filteredIndexes];
}

@end
