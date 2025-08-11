package com.example.fourquadrant.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.fourquadrant.database.AppDatabase;
import com.example.fourquadrant.database.dao.UserDao;
import com.example.fourquadrant.database.entity.UserEntity;

/**
 * 用户数据仓库
 */
public class UserRepository {
    
    private UserDao userDao;
    private LiveData<UserEntity> user;
    
    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        user = userDao.getUser();
    }
    
    // 获取用户信息
    public LiveData<UserEntity> getUser() {
        return user;
    }
    
    // 同步获取用户信息
    public UserEntity getUserSync() {
        return userDao.getUserSync();
    }
    
    // 获取用户创建时间
    public LiveData<Long> getUserCreatedAt() {
        return userDao.getUserCreatedAt();
    }
    
    // 检查用户是否存在
    public boolean isUserExists() {
        return userDao.getUserCount() > 0;
    }
    
    // 创建用户
    public void createUser(String username, String email, String bio) {
        UserEntity user = new UserEntity(username, email, bio);
        insertUser(user);
    }
    
    // 插入或更新用户
    public void insertUser(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insertUser(user);
        });
    }
    
    // 更新用户信息
    public void updateUser(UserEntity user) {
        user.setUpdatedAt(System.currentTimeMillis());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateUser(user);
        });
    }
    
    // 更新用户名
    public void updateUsername(String username) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateUsername(username, System.currentTimeMillis());
        });
    }
    
    // 更新邮箱
    public void updateEmail(String email) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateEmail(email, System.currentTimeMillis());
        });
    }
    
    // 更新个人简介
    public void updateBio(String bio) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateBio(bio, System.currentTimeMillis());
        });
    }
    
    // 更新头像路径
    public void updateAvatarPath(String avatarPath) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateAvatarPath(avatarPath, System.currentTimeMillis());
        });
    }
    
    // 重置用户信息
    public void resetUser() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.deleteUser();
        });
    }
    
    // 初始化默认用户（如果不存在）
    public void initializeDefaultUserIfNotExists() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (userDao.getUserCount() == 0) {
                UserEntity defaultUser = new UserEntity();
                defaultUser.setUsername("用户");
                defaultUser.setEmail("");
                defaultUser.setBio("欢迎使用四象限任务管理工具");
                userDao.insertUser(defaultUser);
            }
        });
    }
    
    // 更新用户信息（批量）
    public void updateUserInfo(String username, String email, String bio) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = userDao.getUserSync();
            if (user == null) {
                // 如果用户不存在，创建新用户
                user = new UserEntity(username, email, bio);
                userDao.insertUser(user);
            } else {
                // 更新现有用户信息
                user.setUsername(username);
                user.setEmail(email);
                user.setBio(bio);
                user.setUpdatedAt(System.currentTimeMillis());
                userDao.updateUser(user);
            }
        });
    }
    
    // 获取用户详细信息（便捷方法）
    public UserInfo getUserInfo() {
        UserEntity user = getUserSync();
        if (user != null) {
            return new UserInfo(
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarPath(),
                user.getCreatedAt()
            );
        }
        return null;
    }
    
    // 用户信息数据类
    public static class UserInfo {
        public final String username;
        public final String email;
        public final String bio;
        public final String avatarPath;
        public final long createdAt;
        
        public UserInfo(String username, String email, String bio, 
                       String avatarPath, long createdAt) {
            this.username = username;
            this.email = email;
            this.bio = bio;
            this.avatarPath = avatarPath;
            this.createdAt = createdAt;
        }
    }
}
