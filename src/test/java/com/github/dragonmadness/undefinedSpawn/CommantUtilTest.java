package com.github.dragonmadness.undefinedSpawn;

import com.github.dragonmadness.undefinedSpawn.command.util.CommandUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class CommantUtilTest {

    @Test
    public void testColorCodeReplacement() {
        Assertions.assertEquals(
                "§7test§fstring§raaaa",
                CommandUtil.translateColorCodes("&7test&fstring&raaaa")
        );
    }

}
