//
//  PanoWbTopView.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseView.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^PanoSelectedBlock)(UITableView *tableView, NSIndexPath *indexPath);

@interface PanoWbTopMoreView : PanoBaseView 
@property (nonatomic, strong, readonly) UITableView *tableView;
@property (nonatomic, copy) NSString *fileName;
@property (nonatomic, copy) NSString *presnterName;

@end

@interface PanoWbTopView : PanoBaseView

@property (nonatomic, strong, readonly) UIButton *prevBtn;
@property (nonatomic, strong, readonly) UIButton *nextBtn;
@property (nonatomic, strong, readonly) UIButton *addBtn;
@property (nonatomic, strong, readonly) UIButton *deleteBtn;
@property (nonatomic, strong, readonly) UILabel * pageNumber;
@property (nonatomic, strong, readonly) UILabel * zoomScale;
@property (nonatomic, strong, readonly) UIButton *moreButton;
@property (nonatomic, strong, readonly) PanoWbTopMoreView *topMoreView;
@property (nonatomic, assign, readonly) BOOL isShowing;

- (void)showMenuViewInView:(UIView *)target selectedBlock:(PanoSelectedBlock)selectedBlock;

- (void)showMenuViewInView:(UIView *)target
                  animated:(BOOL)animated
             selectedBlock:(PanoSelectedBlock)selectedBlock;
- (void)hideMenuView;


@end

NS_ASSUME_NONNULL_END
