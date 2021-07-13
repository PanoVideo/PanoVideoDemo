//
//  PanoArrayDataSource.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoArrayDataSource.h"

@implementation PanoArrayDataSource 

- (instancetype)initWithItems:(NSArray<NSArray<id<PanoItemDelegate>> *> *)items cellIdentifier:(NSString *)cellIdentifier configureCellBlock:(ConfigureCellBlock)configureCellBlock
{
    self = [super init];
    if (self) {
        _items = items;
        _cellIdentifier = cellIdentifier;
        _configureCellBlock = configureCellBlock;
    }
    return self;
}

#pragma mark - UITableViewDataSource


- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.items.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [[self.items objectAtIndex:section] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:self.cellIdentifier forIndexPath:indexPath];
    
    if (self.configureCellBlock) {
        self.configureCellBlock(cell, [self itemAtIndexPath:indexPath]);
    }
    
    return cell;
}

#pragma mark - Private

- (NSObject *)itemAtIndexPath:(NSIndexPath *)indexPath
{
    return [self.items[indexPath.section] objectAtIndex:indexPath.row];
}


@end
