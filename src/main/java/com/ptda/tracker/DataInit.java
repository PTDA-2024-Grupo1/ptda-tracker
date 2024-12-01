package com.ptda.tracker;

import com.ptda.tracker.models.user.Tier;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.admin.AdminService;
import com.ptda.tracker.services.admin.RoleManagementService;
import com.ptda.tracker.services.assistant.AssistantService;
import com.ptda.tracker.services.user.TierService;
import com.ptda.tracker.services.user.DailyLimitService;
import com.ptda.tracker.services.user.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInit {
    private final UserService userService;
    private final TierService tierService;
    private final AssistantService assistantService;
    private final AdminService adminService;
    private final RoleManagementService roleManagementService;
    private final DailyLimitService dailyLimitService;

    @PostConstruct
    public void init() {
        System.out.println("Initializing data...");

        if (tierService.getAll().isEmpty()) {
            System.out.println("No tiers found, creating default tiers...");
            createDefaultTiers();
        }

        Optional<User> user = userService.getByEmail("ratmir.m2004@gmail.com");
        roleManagementService.promoteUserToAssistant(user.get());

        /* if (dailyLimitService.getAll().isEmpty()) {
            System.out.println("No daily limits found, creating default daily limits...");
            createDefaultDailyLimits();
        }
         */
    }

    private void createDefaultTiers() {
        List<Tier> tiers = List.of(
                Tier.builder().name("Bronze").points(0).build(),
                Tier.builder().name("Silver").points(100).build(),
                Tier.builder().name("Gold").points(200).build(),
                Tier.builder().name("Platinum").points(300).build()
        );
        tierService.create(tiers);
    }

    private void createUsers() {
        Tier bronze = tierService.getByName("Bronze");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .name("User " + i)
                    .email("user" + System.currentTimeMillis() + "@gmail.com")
                    .password("password")
                    .tier(bronze)
                    .build();
            users.add(user);
        }
        userService.create(users);
    }

    /* private void createDefaultDailyLimits() {
        List<DailyLimit> dailyLimits = List.of(
                DailyLimit.builder().actionType(ActionType.CREATE_BUDGET).limit(5).tier(tierService.getByName("Bronze")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_EXPENSE).limit(10).tier(tierService.getByName("Bronze")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_BUDGET).limit(10).tier(tierService.getByName("Silver")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_EXPENSE).limit(20).tier(tierService.getByName("Silver")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_BUDGET).limit(15).tier(tierService.getByName("Gold")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_EXPENSE).limit(30).tier(tierService.getByName("Gold")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_BUDGET).limit(20).tier(tierService.getByName("Platinum")).build(),
                DailyLimit.builder().actionType(ActionType.CREATE_EXPENSE).limit(40).tier(tierService.getByName("Platinum")).build()
        );
        dailyLimitService.create(dailyLimits);
    }
    */
}