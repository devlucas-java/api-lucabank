package com.github.devlucasjava.apilucabank.service;

import com.github.devlucasjava.apilucabank.dto.request.CreateTransactionRequest;
import com.github.devlucasjava.apilucabank.dto.response.TransactionResponse;
import com.github.devlucasjava.apilucabank.exception.ResourceNotFoundException;
import com.github.devlucasjava.apilucabank.model.*;
import com.github.devlucasjava.apilucabank.repository.AccountRepository;
import com.github.devlucasjava.apilucabank.repository.TransactionsRepository;
import com.github.devlucasjava.apilucabank.service.validator.AccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountValidator accountValidator;

    private Users user;
    private Account sender;
    private Account receiver;
    private CreateTransactionRequest request;

    @BeforeEach
    void setup() {
        user = new Users();
        user.setId(UUID.randomUUID());
        user.setFirstName("Lucas");

        sender = new Account();
        sender.setId(UUID.randomUUID());
        sender.setBalance(new BigDecimal(1000));
        sender.setUser(user);

        receiver = new Account();
        receiver.setId(UUID.randomUUID());
        receiver.setBalance(new BigDecimal(500));

        Users receiverUser = new Users();
        receiverUser.setId(UUID.randomUUID());
        receiverUser.setFirstName("John");

        receiver.setUser(receiverUser);

        request = new CreateTransactionRequest();
        request.setReceiverId(receiver.getId());
        request.setAmount(new BigDecimal(200));
        request.setDescription("Test transfer");
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        when(accountRepository.findById(receiver.getId()))
                .thenReturn(Optional.of(receiver));

        when(accountRepository.findByUser(user))
                .thenReturn(Optional.of(sender));

        TransactionResponse response =
                transactionService.createTransaction(request, user);

        assertNotNull(response);

        assertEquals(new BigDecimal(800), sender.getBalance());
        assertEquals(new BigDecimal(700), receiver.getBalance());

        verify(transactionsRepository, times(1)).save(any(Transactions.class));
        verify(accountRepository, times(1)).save(sender);
        verify(accountRepository, times(1)).save(receiver);

        verify(accountValidator).accountNotBlocked(sender);
        verify(accountValidator).accountNotBlocked(receiver);
        verify(accountValidator).sufficientFunds(sender, request.getAmount());
        verify(accountValidator).maximumAmountNotExceeded(sender, request.getAmount());
        verify(accountValidator).accountEqual(receiver.getId(), sender.getId());
    }

    @Test
    void shouldThrowExceptionWhenReceiverNotFound() {
        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(request, user));
    }

    @Test
    void shouldThrowExceptionWhenSenderNotFound() {
        when(accountRepository.findById(receiver.getId()))
                .thenReturn(Optional.of(receiver));

        when(accountRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(request, user));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        when(accountRepository.findById(receiver.getId()))
                .thenReturn(Optional.of(receiver));

        when(accountRepository.findByUser(user))
                .thenReturn(Optional.of(sender));

        doThrow(new RuntimeException("Insufficient funds"))
                .when(accountValidator)
                .sufficientFunds(sender, request.getAmount());

        assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(request, user));
    }

    @Test
    void shouldThrowExceptionWhenAccountBlocked() {
        when(accountRepository.findById(receiver.getId()))
                .thenReturn(Optional.of(receiver));

        when(accountRepository.findByUser(user))
                .thenReturn(Optional.of(sender));

        doThrow(new RuntimeException("Account blocked"))
                .when(accountValidator)
                .accountNotBlocked(sender);

        assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(request, user));
    }
}