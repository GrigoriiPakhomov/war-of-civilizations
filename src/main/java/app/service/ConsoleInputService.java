package app.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * Сервис консольного ввода и базовой валидации пользовательских параметров.
 */
@Slf4j
public class ConsoleInputService {

    /**
     * Считывает целое число в указанном диапазоне.
     *
     * @param scanner источник ввода
     * @param prompt текст приглашения
     * @param min минимальное значение
     * @param max максимальное значение
     * @return корректное целое значение
     */
    public int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    log.warn("Значение должно быть в диапазоне от {} до {}", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                log.warn("Введите целое число");
            }
        }
    }

    /**
     * Считывает процент в диапазоне от 0 до 100.
     *
     * @param scanner источник ввода
     * @param prompt текст приглашения
     * @return значение в диапазоне от 0.0 до 1.0
     */
    public double readPercent(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value < 0 || value > 100) {
                    log.warn("Процент должен быть в диапазоне от 0 до 100");
                    continue;
                }
                return value / 100.0;
            } catch (NumberFormatException e) {
                log.warn("Введите число");
            }
        }
    }
}
