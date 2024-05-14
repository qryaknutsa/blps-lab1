package com.example.blpslab1.service;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.BitronixTransactionManager;

import com.example.blpslab1.dto.RegUserDTO;
import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.subModel.Role;
import com.example.blpslab1.repo.*;


import javax.transaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static double subscription_price = 1500;
    private final PasswordEncoder passwordEncoder;
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
//        fileRepo.deleteAll(fileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).toList());
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
    //TransactionFailedException
    //SubAlreadyExistsException
    public void buySub(String username) throws SystemException, javax.transaction.SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            if (user.getSubscription()) {
                throw new SubAlreadyExistsException("Подписка уже куплена");
            } else {
                if (user.getWallet() < subscription_price)
                    throw new OutOfBalanceException("Не хватает денег");
                else {
                    tm.begin();
                    user.setWallet(user.getWallet() - subscription_price);
                    user.setSubscription(true);
                    userRepo.save(user);
                    tm.commit();
                }
            }
        }catch (Exception e){
            tm.rollback();
            throw new TransactionFailedException("Подписка не оформлена");
        }
//        } catch (HeuristicRollbackException | HeuristicMixedException | NotSupportedException e) {
//            tm.rollback();
//            throw new TransactionFailedException("Подписка не оформлена");
//        } catch (RollbackException | javax.transaction.NotSupportedException e) {
//            tm.rollback();
//            throw new TransactionFailedException("Подписка не оформлена, откат не произошел");
//        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    //TransactionFailedException
    public void cancelSub(String username) throws SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            tm.begin();
            user.setSubscription(false);
            userRepo.save(user);
            tm.commit();
        } catch (HeuristicRollbackException | HeuristicMixedException | NotSupportedException e) {
            tm.rollback();
            throw new TransactionFailedException("Подписка не отменена");
        } catch (RollbackException e) {
            throw new TransactionFailedException("Подписка не куплена, откат не произошел");
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    //TransactionFailedException
    public void putMoney(String username, Double sum) throws SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            tm.begin();
            user.setWallet(user.getWallet() + sum);
            userRepo.save(user);
            tm.commit();
        } catch (HeuristicRollbackException | HeuristicMixedException | NotSupportedException e) {
            tm.rollback();
            throw new TransactionFailedException("Деньги нне вышло положить на счёт");
        } catch (RollbackException e) {
            throw new TransactionFailedException("Деньги не вышло положить на счёт, откат не произошел");
        }
    }

    //UserNotFoundException
    //OutOfBalanceException
    //TransactionFailedException

    @Transactional
    public void withdrawMoney(String username, Double sum) throws SystemException {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            user.setWallet(user.getWallet() - sum);
            userRepo.save(user);
            method();
        }catch (Exception e) {
            throw new TransactionFailedException("Деньги не снялись со счёта");
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
}
