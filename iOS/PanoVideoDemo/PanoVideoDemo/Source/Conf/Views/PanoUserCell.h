//
//  PanoUserCell.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface PanoUserCell : UITableViewCell

@property (nonatomic, strong, readonly) UILabel *titleLabel;

@property (nonatomic, strong, readonly) UIButton *iconButton;

@property (nonatomic, strong, readonly) UIImageView *audioImageView;

@property (nonatomic, strong, readonly) UIImageView *videoImageView;



@end

NS_ASSUME_NONNULL_END
