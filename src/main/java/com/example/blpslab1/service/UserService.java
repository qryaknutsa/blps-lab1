package com.example.blpslab1.service;

import com.example.blpslab1.dto.RegUserDTO;
import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;
import com.example.blpslab1.subModel.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class UserService {
    public static double subscription_price = 15;
    public static double subscriptionDurationInDays = 1;

    //    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;


    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    //UserNotFoundException
    public User getUser(String username) {
        return userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);

    }


    //UserAlreadyExistException
    public void saveUser(RegUserDTO userDTO) {
//        User user = new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), userDTO.getRole());
        User user = new User(userDTO.getUsername(), userDTO.getPassword(), userDTO.getRole());

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
//        fileRepo.deleteAll(fileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).toList());
    }

    //UserNotFoundException
    public void changePassword(User user) {
        User user1 = userRepo.findUserByUsername(user.getUsername()).orElseThrow(UserNotFoundException::new);
//        user1.setPassword(passwordEncoder.encode(user.getPassword()));
        user1.setPassword(user.getPassword());

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
        if (user.getSubscription()) {
            throw new SubAlreadyExistsException("Подписка уже куплена");
        } else {
            if (user.getWallet() < subscription_price)
                throw new OutOfBalanceException("Не хватает денег");
            else {
                user.setWallet(user.getWallet() - subscription_price);
                user.setSubscription(true);
                userRepo.save(user);
            }
        }

    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void cancelSub(String username) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        user.setSubscription(false);
        userRepo.save(user);
    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void putMoney(String username, Double sum) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        user.setWallet(user.getWallet() + sum);
        userRepo.save(user);

    }

    //UserNotFoundException
    //OutOfBalanceException
    @Transactional
    public void withdrawMoney(String username, Double sum) {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            user.setWallet(user.getWallet() - sum);
            userRepo.save(user);
//            method();
        } catch (Exception e) {
            throw new TransactionFailedException("Деньги не снялись со счёта");
        }
    }

    void method() {
        throw new RuntimeException();
    }


    //        @Scheduled(cron = "*/2 * * * * *")
//    @Scheduled(cron = "0 0 0 * * ?")
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
