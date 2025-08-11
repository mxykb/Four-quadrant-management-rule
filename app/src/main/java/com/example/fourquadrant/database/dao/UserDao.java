package com.example.fourquadrant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.entity.UserEntity;

/**
 * 用户信息数据访问对象
 */
@Dao
public interface UserDao {
    
    // 插入或更新用户信息
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);
    
    // 更新用户信息
    @Update
    void updateUser(UserEntity user);
    
    // 查询用户信息（单用户系统）
    @Query("SELECT * FROM user_info WHERE id = 'default_user'")
    LiveData<UserEntity> getUser();
    
    // 同步查询用户信息
    @Query("SELECT * FROM user_info WHERE id = 'default_user'")
    UserEntity getUserSync();
    
    // 检查用户是否存在
    @Query("SELECT COUNT(*) FROM user_info WHERE id = 'default_user'")
    int getUserCount();
    
    // 更新用户名
    @Query("UPDATE user_info SET username = :username, updated_at = :updateTime WHERE id = 'default_user'")
    void updateUsername(String username, long updateTime);
    
    // 更新邮箱
    @Query("UPDATE user_info SET email = :email, updated_at = :updateTime WHERE id = 'default_user'")
    void updateEmail(String email, long updateTime);
    
    // 更新个人简介
    @Query("UPDATE user_info SET bio = :bio, updated_at = :updateTime WHERE id = 'default_user'")
    void updateBio(String bio, long updateTime);
    
    // 更新头像路径
    @Query("UPDATE user_info SET avatar_path = :avatarPath, updated_at = :updateTime WHERE id = 'default_user'")
    void updateAvatarPath(String avatarPath, long updateTime);
    
    // 删除用户信息（重置）
    @Query("DELETE FROM user_info WHERE id = 'default_user'")
    void deleteUser();
    
    // 获取用户创建时间
    @Query("SELECT created_at FROM user_info WHERE id = 'default_user'")
    LiveData<Long> getUserCreatedAt();
}
