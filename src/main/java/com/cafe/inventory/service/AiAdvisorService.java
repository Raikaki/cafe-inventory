package com.cafe.inventory.service;

import com.cafe.inventory.dto.ForecastDtos.ForecastItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Produces a natural-language restocking recommendation.
 * If a free Google Gemini API key is configured (env GEMINI_API_KEY) it calls
 * Gemini; otherwise it falls back to a deterministic rule-based summary so the
 * feature always works — even on the free tier without any key.
 */
@Slf4j
@Service
public class AiAdvisorService {

    @Value("${app.ai.gemini-key:}")
    private String geminiKey;

    @Value("${app.ai.gemini-model:gemini-1.5-flash}")
    private String geminiModel;

    private final RestClient restClient = RestClient.create();

    public boolean isLlmEnabled() {
        return geminiKey != null && !geminiKey.isBlank();
    }

    public String buildAdvice(List<ForecastItem> items, int lookbackDays, int horizonDays) {
        if (isLlmEnabled()) {
            try {
                return callGemini(items, lookbackDays, horizonDays);
            } catch (Exception e) {
                log.warn("Gemini call failed, falling back to rule-based advice: {}", e.getMessage());
            }
        }
        return ruleBased(items, horizonDays);
    }

    // ---------- Gemini ----------
    @SuppressWarnings("unchecked")
    private String callGemini(List<ForecastItem> items, int lookbackDays, int horizonDays) {
        String prompt = buildPrompt(items, lookbackDays, horizonDays);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        Map<String, Object> resp = restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private String buildPrompt(List<ForecastItem> items, int lookbackDays, int horizonDays) {
        String data = items.stream()
                .filter(i -> !"OK".equals(i.status()) && !"IDLE".equals(i.status()))
                .limit(15)
                .map(i -> String.format("- %s (%s): tồn %s %s, dùng TB %s/ngày, hết sau %s ngày, cần nhập thêm %s",
                        i.materialName(), i.materialCode(), i.currentQty(), i.unit(),
                        i.avgDailyUsage(),
                        i.daysToStockout() == null ? "∞" : i.daysToStockout(),
                        i.recommendedReorderQty()))
                .collect(Collectors.joining("\n"));
        if (data.isBlank()) data = "(Không có nguyên vật liệu nào ở mức cảnh báo)";
        return "Bạn là chuyên gia quản lý kho cho một quán cafe. Dựa trên dữ liệu dự báo "
                + lookbackDays + " ngày gần đây (tầm nhìn " + horizonDays + " ngày) dưới đây, "
                + "hãy đưa ra nhận định ngắn gọn bằng tiếng Việt (tối đa 6 gạch đầu dòng): "
                + "nguyên vật liệu nào cần nhập gấp, gợi ý số lượng, và một lời khuyên tối ưu tồn kho.\n\n"
                + data;
    }

    // ---------- Rule-based fallback ----------
    private String ruleBased(List<ForecastItem> items, int horizonDays) {
        List<ForecastItem> critical = items.stream().filter(i -> "CRITICAL".equals(i.status())).toList();
        List<ForecastItem> warning = items.stream().filter(i -> "WARNING".equals(i.status())).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Phân tích dự báo (tầm nhìn ").append(horizonDays).append(" ngày):\n");
        if (critical.isEmpty() && warning.isEmpty()) {
            sb.append("• Tồn kho đang ở mức an toàn, chưa có nguyên vật liệu nào cần nhập gấp.\n");
        }
        for (ForecastItem i : critical) {
            sb.append(String.format("• ⚠ KHẨN: %s sắp hết (còn ~%s ngày). Nên nhập thêm %s %s.\n",
                    i.materialName(),
                    i.daysToStockout() == null ? "?" : i.daysToStockout(),
                    i.recommendedReorderQty(), i.unit()));
        }
        for (ForecastItem i : warning) {
            sb.append(String.format("• Theo dõi: %s dưới định mức/sắp thấp. Cân nhắc nhập %s %s.\n",
                    i.materialName(), i.recommendedReorderQty(), i.unit()));
        }
        sb.append("• Gợi ý: đặt hàng trước ").append(7).append(" ngày so với ngày dự kiến hết để tránh đứt hàng.");
        return sb.toString();
    }
}
