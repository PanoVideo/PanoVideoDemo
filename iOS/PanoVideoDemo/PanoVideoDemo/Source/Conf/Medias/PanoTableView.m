//
//  PanoRosterView.m
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "PanoTableView.h"
#import "PanoUserCell.h"

@implementation PanoTableView

- (void)initViews {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.rowHeight = 55;
    _tableView.tableFooterView = [[UIView alloc] init];
    _tableView.showsVerticalScrollIndicator = false;
    _tableView.backgroundColor = [UIColor pano_colorWithHexString:@"#F5F7FF"];
    self.backgroundColor = [UIColor whiteColor];
    [self addSubview:_tableView];
    [self initHeaderView];
}

- (void)initHeaderView {
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectZero];
    _searchBar = [[UISearchBar alloc] initWithFrame:CGRectZero];
    [headerView addSubview:_searchBar];
    [_searchBar mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(0);
    }];
    _tableView.tableHeaderView = headerView;
}

- (void)initConstraints {
    [_tableView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
    [_tableView.tableHeaderView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(self);
    }];
    [self.tableView layoutIfNeeded];
    [self.tableView.tableHeaderView layoutIfNeeded];
}

- (void)setShowSearchBar:(BOOL)showSearchBar {
    if (showSearchBar) {
        [self initHeaderView];
        [self initConstraints];
    } else {
        _tableView.tableHeaderView  = [UIView new];
    }
}

@end
