package nextstep.subway.station;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.station.ui.StationController;
import nextstep.subway.utils.TestFactory;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

	@DisplayName("지하철역을 생성한다.")
	@Test
	void createStation() {
		// given, when
		ExtractableResponse<Response> response = 지하철역_생성_요청("간암역");

		// then
		지하철역_생성_완료됨(response);
	}

	@DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
	@Test
	void createStationWithDuplicateName() {
		// given
		String stationName = "강남역";
		지하철역_등록되어_있음(stationName);

		// when
		ExtractableResponse<Response> response = 지하철역_생성_요청(stationName);

		// then
		지하철역_생성_실패함(response);
	}

	@DisplayName("지하철역을 조회한다.")
	@Test
	void getStations() {
		/// given
		ExtractableResponse<Response> createResponse1 = 지하철역_등록되어_있음("강남역");
		ExtractableResponse<Response> createResponse2 = 지하철역_등록되어_있음("역삼역");

		// when
		ExtractableResponse<Response> response = 지하철역_조회_요청();

		// then
		assertAll(
			() -> 지하철역_조회_응답됨(response),
			() -> 지하철역_조회_내용_포함됨(response, Arrays.asList(createResponse1, createResponse2))
		);
	}

	@DisplayName("지하철역을 제거한다.")
	@Test
	void deleteStation() {
		// given
		ExtractableResponse<Response> createResponse = 지하철역_등록되어_있음("강남역");

		// when
		ExtractableResponse<Response> response = 지하철역_제거_요청(createResponse);

		// then
		지하철역_제거됨(response);
	}

	public static ExtractableResponse<Response> 지하철역_등록되어_있음(String stationName) {
		Map<String, String> params = new HashMap<>();
		params.put("name", stationName);
		return TestFactory.create(StationController.BASE_URI, params);
	}

	private ExtractableResponse<Response> 지하철역_제거_요청(ExtractableResponse<Response> given) {
		String uri = given.header("Location");
		return TestFactory.delete(uri);
	}

	private void 지하철역_제거됨(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	private void 지하철역_조회_응답됨(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
	}

	private void 지하철역_조회_내용_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> givens) {
		List<Long> expectedLineIds = givens.stream()
			.map(it -> Long.parseLong(it.header("Location").split("/")[2]))
			.collect(Collectors.toList());
		List<Long> resultLineIds = response.jsonPath().getList(".", StationResponse.class)
			.stream()
			.map(StationResponse::getId)
			.collect(Collectors.toList());
		assertThat(resultLineIds).containsAll(expectedLineIds);
	}

	private ExtractableResponse<Response> 지하철역_조회_요청() {
		return TestFactory.findAll(StationController.BASE_URI);
	}

	private void 지하철역_생성_실패함(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	private ExtractableResponse<Response> 지하철역_생성_요청(String stationName) {
		Map<String, String> params = new HashMap<>();
		params.put("name", stationName);
		return TestFactory.create(StationController.BASE_URI, params);
	}

	private void 지하철역_생성_완료됨(ExtractableResponse<Response> response) {
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
			() -> assertThat(response.header("Location")).isNotBlank()
		);
	}

}
