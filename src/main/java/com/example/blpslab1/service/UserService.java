package com.example.blpslab1.service;

import bitronix.tm.BitronixTransactionManager;
import com.example.blpslab1.dto.RegUserDTO;
import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.model.subModel.Role;
import com.example.blpslab1.repo.*;

import javax.transaction.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@EnableTransactionManagement
public class UserService implements UserDetailsService {
    public static double subscription_price = 15;
    public static double subscriptionDurationInDays = 1;

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final FileService fileService;


    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    //UserNotFoundException
    public User getUser(String username) {
        return userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);

    }


    //UserAlreadyExistException
    public void saveUser(RegUserDTO userDTO) {
        User user = new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), userDTO.getRole());
        try {
            userRepo.findUserByUsername(userDTO.getUsername()).orElseThrow(UserNotFoundException::new);
            throw new UserAlreadyExistsException("User с таким username уже существует, user не сохранен");
        } catch (UserNotFoundException e) {
            if (userDTO.getRole() == Role.ADMIN) user.setSubscription(true);
            userRepo.save(user);
        }
    }


    //UserNotFoundException
    public void delete(String username) {
        User user;
        user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        userRepo.delete(user);
        fileService.deleteAllFilesByUsername(username);
    }

    //UserNotFoundException
    public void changePassword(User user) {
        User user1 = userRepo.findUserByUsername(user.getUsername()).orElseThrow(UserNotFoundException::new);
        user1.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user1);

    }

    //UserNotFoundException -- oldUsername
    //UserAlreadyExistException
    public void changeUsername(String oldUsername, User user) {
        User userTest = userRepo.findUserByUsername(oldUsername).orElseThrow(UserNotFoundException::new);
        try {
            userRepo.findUserByUsername(user.getUsername()).orElseThrow(UserNotFoundException::new);
            throw new UserAlreadyExistsException("User с таким username уже существует, username пользователя под именем " + oldUsername + " не получилось");
        } catch (UserNotFoundException e) {
            userTest.setUsername(user.getUsername());
            userRepo.save(userTest);
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    //SubAlreadyExistsException
    @Transactional
    public void buySub(String username) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            if (user.getSubscription()) {
                throw new SubAlreadyExistsException("Подписка уже куплена");
            } else {
                if (user.getWallet() < subscription_price)
                    throw new OutOfBalanceException("Не хватает денег");
                else {
                    user.setWallet(user.getWallet() - subscription_price);
                    user.setSubscription(true);
                    user.setSubDate(Date.valueOf(LocalDate.now()));
                    userRepo.save(user);
                }
            }
        } catch (UserNotFoundException e) {
            throw new RuntimeException("Подписка не оформлена. Ошибка: " + e.getMessage());
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void cancelSub(String username) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            user.setSubscription(false);
            user.setSubDate(null);
            userRepo.save(user);
        } catch (UserNotFoundException e) {
            throw new RuntimeException("Подписка не отменена. Ошибка: " + e.getMessage());
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void putMoney(String username, Double sum) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            user.setWallet(user.getWallet() + sum);
            userRepo.save(user);
        } catch (UserNotFoundException e) {
            throw new RuntimeException("Деньги не вышло положить на счёт. Ошибка: " + e.getMessage());
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void withdrawMoney(String username, Double sum) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            if (user.getWallet() < sum)
                throw new OutOfBalanceException("Не хватает денег");
            user.setWallet(user.getWallet() - sum);
            userRepo.save(user);
//            method();
        } catch (UserNotFoundException e) {
            throw new RuntimeException("Деньги не снялись со счёта. Ошибка: " + e.getMessage());
        }
    }

    void method() {
        throw new RuntimeException();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User not found");
        }
    }


    //    @Scheduled(cron = "*/2 * * * * *")
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void renew_sub() {
        List<User> all_users = getAllUsers();
        Date currentDate = Date.valueOf(LocalDate.now());

        for (User user : all_users) {
            if (user.getSubscription()) {
                Date startTrial = user.getSubDate();
                long diffInMillies = Math.abs(currentDate.getTime() - startTrial.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if (diffInDays >= subscriptionDurationInDays) {
//                if (true) {
                    try {
                        withdrawMoney(user.getUsername(), subscription_price);
                        user.setSubDate(currentDate); // Обновляем дату подписки
                        userRepo.save(user); // Метод для обновления данных пользователя в базе
                        System.out.println("Подписка обновлена, деньги сняты со счёта.");
                    } catch (OutOfBalanceException e) {
                        user.setSubDate(null);
                        user.setSubscription(false);
                        userRepo.save(user);
                        System.out.println("Не удалось обновить подписку, не хватило средств на кошельке.");
                    }
                }
            }
        }
    }
}
