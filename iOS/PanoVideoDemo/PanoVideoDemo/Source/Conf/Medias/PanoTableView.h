//
//  PanoRosterView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "PanoBaseView.h"

NS_ASSUME_NONNULL_BEGIN

/// 用户列表View
@interface PanoTableView : PanoBaseView

@property (nonatomic, strong) UITableView *tableView;

@property (nonatomic, strong) UISearchBar *searchBar;

@property (nonatomic, assign) BOOL showSearchBar;

@end

NS_ASSUME_NONNULL_END
