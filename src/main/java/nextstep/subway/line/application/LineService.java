package nextstep.subway.line.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.exception.AppException;
import nextstep.subway.exception.ErrorCode;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.LineUpdateRequest;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;

@Service
@Transactional
public class LineService {

	private final LineRepository lineRepository;
	private final StationRepository stations;

	public LineService(LineRepository lineRepository, StationRepository stations) {
		this.lineRepository = lineRepository;
		this.stations = stations;
	}

	public LineResponse saveLine(LineRequest request) {
		validateDuplication(request.getName());
		Line persistLine = lineRepository.save(request.toLine());
		return LineResponse.of(persistLine);
	}

	private void validateDuplication(String name) {
		if (lineRepository.existsByName(name)) {
			throw new AppException(ErrorCode.DUPLICATE_INPUT, name + "은 중복입니다");
		}
	}

	public List<LineResponse> getLines() {
		return LineResponse.ofList(lineRepository.findAll());
	}

	public LineResponse getLineById(Long id) {
		Line line = getById(id);
		return LineResponse.of(line);
	}

	public LineResponse modify(Long id, LineUpdateRequest lineRequest) {
		Line line = getById(id);
		line.update(lineRequest.toLine());
		return LineResponse.of(line);
	}

	private Line getById(Long id) {
		return lineRepository.findById(id)
			.orElseThrow(() -> new AppException(ErrorCode.WRONG_INPUT, id + "는 존재하지 않습니다"));
	}

	public void deleteLineById(Long id) {
		Line line = getById(id);
		lineRepository.delete(line);
	}

	public LineResponse updateSections(Long id, SectionRequest sectionRequest) {
		Station upStation = stations.findById(sectionRequest.getUpStationId()).get();
		Station downStation = stations.findById(sectionRequest.getDownStationId()).get();
		Section section = Section.of(null, upStation, downStation, sectionRequest.getDistance());
		Line line = lineRepository.findById(id).get();
		line.updateSections(section);
		return LineResponse.of(line);
	}

}

