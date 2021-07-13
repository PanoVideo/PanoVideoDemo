//
//  PanoWbTopView.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoWbTopView.h"
#import "UIView+Extension.h"
#import "PanoMenuCell.h"

CGFloat RowHeight = 30.0;
CGFloat SectionHeaderHeight = 8;

@interface PanoWbTopMoreView () <UITableViewDelegate>
@property (nonatomic, strong) UILabel * wbNameLabel;
@property (nonatomic, strong) UILabel * wbPresenterLabel;
@property (nonatomic, copy) PanoSelectedBlock block;
@end

@implementation PanoWbTopMoreView

- (void)initViews {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.rowHeight = RowHeight;
    _tableView.tableHeaderView = [self headerView];
    _tableView.showsVerticalScrollIndicator = false;
    _tableView.backgroundColor = [UIColor pano_colorWithHexString:@"f1f1f1"];
    _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    _tableView.bounces = false;
    [self addSubview:_tableView];
}

- (void)initConstraints {
    [_tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
    }];
}
- (void)reloadViews {
    NSDictionary *leftAttributes = @{
        NSForegroundColorAttributeName : [UIColor grayColor],
        NSFontAttributeName: [UIFont systemFontOfSize:PanoFontSize_Little]
    };
    NSDictionary *rightAttributes = @{
        NSForegroundColorAttributeName : [UIColor blackColor],
        NSFontAttributeName: [UIFont systemFontOfSize:PanoFontSize_Little]
    };
    NSMutableAttributedString *fileName = [[NSMutableAttributedString alloc] initWithAttributedString:[[NSAttributedString alloc] initWithString:NSLocalizedString(@"Current whiteboard", nil) attributes:leftAttributes]];
    [fileName appendAttributedString:[[NSAttributedString alloc] initWithString:_fileName ?: @"" attributes:rightAttributes]];
    _wbNameLabel.attributedText = fileName;
    
    NSMutableAttributedString *presnterName = [[NSMutableAttributedString alloc] initWithAttributedString:[[NSAttributedString alloc] initWithString:NSLocalizedString(@"Current Presenter", nil) attributes:leftAttributes]];
    [presnterName appendAttributedString:[[NSAttributedString alloc] initWithString:_presnterName ?: @"" attributes:rightAttributes]];
    _wbPresenterLabel.attributedText = presnterName;
//    [self.tableView reloadData];
}

- (UIView *)headerView {
    UIView *header = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 150, 52)];
    {
        _wbNameLabel = [[UILabel alloc] init];
        [header addSubview:_wbNameLabel];
        
        _wbPresenterLabel = [[UILabel alloc] init];
        [header addSubview:_wbPresenterLabel];
    }
    {
        [_wbNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.left.right.mas_equalTo(header).insets(UIEdgeInsetsMake(DefaultFixSpace, DefaultFixSpace, 0, DefaultFixSpace));
        }];
        [_wbPresenterLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.left.right.mas_equalTo(header).insets(UIEdgeInsetsMake(0, DefaultFixSpace, DefaultFixSpace, DefaultFixSpace));
            make.top.mas_equalTo(_wbNameLabel.mas_bottom).mas_offset(DefaultFixSpace);
            make.height.mas_equalTo(_wbNameLabel);
        }];
    }
    header.backgroundColor = [UIColor whiteColor];
    return header;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:true];
    if (self.block) {
        self.block(tableView, indexPath);
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, UIScreen.mainScreen.bounds.size.width * 0.5, SectionHeaderHeight)];
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return SectionHeaderHeight;
}


@end


@interface PanoWbTopView ()
@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UIView *backView;

@end

@implementation PanoWbTopView

- (void)initViews {
    _contentView = [[UIView alloc] init];
    _contentView.backgroundColor = [UIColor pano_colorWithHexString:@"f1f1f1"];
    _contentView.layer.cornerRadius = 5;
    _contentView.layer.masksToBounds = true;
    [self addSubview:_contentView];
    
//    _prevBtn = [self buttonWithImage:[UIImage imageNamed:@"btn.whiteboard.prev"] hightedImage:nil];
//    [self addSubview:_prevBtn];
    
    _pageNumber = [[UILabel alloc] init];
    _pageNumber.font = [UIFont systemFontOfSize:PanoFontSize_Little];
    _pageNumber.text = @"1/1";
    _pageNumber.textAlignment = NSTextAlignmentCenter;
    [self addSubview:_pageNumber];
    
    _zoomScale = [[UILabel alloc] init];
    _zoomScale.font = [UIFont systemFontOfSize:PanoFontSize_Little];
    _zoomScale.text = @"100%";
    _zoomScale.textAlignment = NSTextAlignmentCenter;
    [_zoomScale sizeToFit];
    [self addSubview:_zoomScale];
    
    _moreButton = [self buttonWithImage:[UIImage imageNamed:@"btn.whiteboard.more"] hightedImage:nil];
    [self addSubview:_moreButton];
    
    _topMoreView = [[PanoWbTopMoreView alloc] init];
    
//    _nextBtn = [self buttonWithImage:[UIImage imageNamed:@"btn.whiteboard.next"] hightedImage:nil];
//    [self addSubview:_nextBtn];
//
//    _addBtn = [self buttonWithImage:[UIImage imageNamed:@"btn.whiteboard.add"] hightedImage:nil];
//    [self addSubview:_addBtn];
//
//    _deleteBtn = [self buttonWithImage:[UIImage imageNamed:@"btn.whiteboard.subtract"] hightedImage:nil];
//    [self addSubview:_deleteBtn];
}

- (UIButton *)buttonWithImage:(UIImage *)image hightedImage:(UIImage *)selectedImage {
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
    [btn setImage:image forState:UIControlStateNormal];;
    [btn setImage:selectedImage forState:UIControlStateSelected];
    [btn sizeToFit];
    return btn;
}

- (void)initConstraints {
    [_contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
        make.height.mas_equalTo(30);
    }];
    
//    [_prevBtn mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.size.mas_equalTo(CGSizeMake(24, 24));
//        make.centerY.mas_equalTo(_contentView);
//        make.left.mas_equalTo(_contentView).mas_offset(LittleFixSpace);
//    }];
    
    [_pageNumber mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(_contentView);
        make.left.mas_equalTo(_contentView).mas_offset(LittleFixSpace);
        make.width.mas_equalTo(40);
        make.height.mas_equalTo(25);
    }];
    
//    [_nextBtn mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.size.mas_equalTo(_prevBtn);
//        make.centerY.mas_equalTo(_contentView);
//        make.left.mas_equalTo(_pageNumber.mas_right).mas_offset(LittleFixSpace);
//    }];
//
//    [_addBtn mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.size.mas_equalTo(_prevBtn);
//        make.centerY.mas_equalTo(_contentView);
//        make.left.mas_equalTo(_nextBtn.mas_right).mas_offset(DefaultFixSpace);
//    }];
//
//    [_deleteBtn mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.size.mas_equalTo(_prevBtn);
//        make.centerY.mas_equalTo(_contentView);
//        make.left.mas_equalTo(_addBtn.mas_right).mas_offset(DefaultFixSpace);
//    }];
    
    [_zoomScale mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(_contentView);
        make.left.mas_equalTo(_pageNumber.mas_right).mas_offset(DefaultFixSpace);
        make.width.height.mas_equalTo(_pageNumber);
    }];
    
    [_moreButton mas_makeConstraints:^(MASConstraintMaker *make) {
       make.size.mas_equalTo(CGSizeMake(25, 25));
       make.centerY.mas_equalTo(_contentView);
       make.left.mas_equalTo(_zoomScale.mas_right).mas_offset(DefaultFixSpace);
       make.right.mas_equalTo(_contentView).mas_offset(-DefaultFixSpace);
    }];
}

- (void)reloadView {

}

- (void)showMenuViewInView:(UIView *)target selectedBlock:(nonnull PanoSelectedBlock)selectedBlock {
    [self showMenuViewInView:target animated:true selectedBlock:selectedBlock];
}
- (void)showMenuViewInView:(UIView *)target animated:(BOOL)animated selectedBlock:(PanoSelectedBlock)selectedBlock
{
    _isShowing = true;
    
    _topMoreView.block = [selectedBlock copy];
    if (![target.subviews containsObject:_topMoreView]) {
        [target addSubview:_topMoreView];
    }
    if (!_backView) {
        UIView *backView = [[UIView alloc] initWithFrame:CGRectZero];
        backView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.15];
        [backView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideMenuView)]];
        [target insertSubview:backView belowSubview:_topMoreView];
        [backView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.mas_equalTo(target);
        }];
        _backView = backView;
    }
    [self.topMoreView reloadViews];
    if (animated) {
        [_topMoreView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(self);
            make.top.mas_equalTo(self.mas_bottom).mas_offset(SectionHeaderHeight);
            make.width.mas_equalTo(self);
            make.height.mas_equalTo(0);
        }];
        [self performSelector:@selector(showMenuAnimate) withObject:nil afterDelay:0.1];
    } else {
        [self showMenuAnimate];
    }
}

- (void)showMenuAnimate;
{
    CGFloat height = 52;
    NSInteger section = [_topMoreView.tableView.dataSource numberOfSectionsInTableView:_topMoreView.tableView];
    for (NSInteger i = 0; i<section; i++) {
        CGFloat rowNumbers = [_topMoreView.tableView.dataSource tableView:_topMoreView.tableView numberOfRowsInSection:i];
        height += rowNumbers * RowHeight + SectionHeaderHeight;
    }
    [_topMoreView mas_updateConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(height);
    }];
    [_topMoreView.superview pano_autoLayout];
}

- (void)hideMenuView {
    _isShowing = false;
    [_topMoreView mas_updateConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(0);
    }];
    [_topMoreView.superview pano_autoLayout];
    [_backView removeFromSuperview];
    _backView = nil;
}

@end
