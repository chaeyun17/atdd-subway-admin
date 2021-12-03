package nextstep.subway.line.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.exception.AppException;
import nextstep.subway.exception.ErrorCode;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.section.application.SectionService;
import nextstep.subway.section.domain.Section;
import nextstep.subway.section.dto.SectionRequest;

@Service
@Transactional
public class LineService {

	private final LineRepository lineRepository;
	private final SectionService sectionService;

	public LineService(LineRepository lineRepository, SectionService sectionService) {
		this.lineRepository = lineRepository;
		this.sectionService = sectionService;
	}

	public LineResponse saveLine(LineRequest request) {
		validateDuplication(request.getName());
		Line persistLine = lineRepository.save(request.toLine());
		Section section = saveSection(request);
		persistLine.addSection(section);
		return LineResponse.of(persistLine);
	}

	private Section saveSection(LineRequest lineRequest) {
		SectionRequest sectionRequest = new SectionRequest(
			lineRequest.getUpStationId(),
			lineRequest.getDownStationId(),
			lineRequest.getDistance());
		return sectionService.saveSection(sectionRequest);
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

	public LineResponse modify(Long id, LineRequest lineRequest) {
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
}
