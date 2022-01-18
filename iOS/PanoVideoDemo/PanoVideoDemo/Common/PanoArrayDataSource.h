//
//  PanoArrayDataSource.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "PanoItemDelegate.h"

NS_ASSUME_NONNULL_BEGIN

typedef void (^ConfigureCellBlock)(id cell, id object);

@interface PanoArrayDataSource : NSObject <UITableViewDataSource>

@property (strong, nonatomic) NSArray<NSArray<id<PanoItemDelegate>> *> *items;
@property (copy, nonatomic) NSString *cellIdentifier;
@property (copy, nonatomic) ConfigureCellBlock configureCellBlock;

- (instancetype)initWithItems:(NSArray<NSArray<id<PanoItemDelegate>> *> *)items cellIdentifier:(NSString *)cellIdentifier configureCellBlock:(ConfigureCellBlock)configureCellBlock;

@end

NS_ASSUME_NONNULL_END
